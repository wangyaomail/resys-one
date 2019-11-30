package xmt.resys.common.dao.l2;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Data;
import xmt.resys.common.bean.job.QueueJob;
import xmt.resys.common.dao.l1.BaseKVDao;
import xmt.resys.common.server.QueueServer;

/**
 * activemq的消费基类
 * @WARN 注意activemq的消费者线程只能负责接收和转发，不可负责具体的工作任务，因为consumer的数量有限制，会降低性能
 */
public abstract class BaseActiveMQDao extends BaseKVDao<String, byte[]> {
    @Autowired
    protected QueueServer queueServer; // 保证性能，所有对象解析都放在这里执行
    protected static Connection _connection = null; // 系统内只需要有一份
    protected Session _session_producer = null;
    protected Session _session_consumer = null;
    protected Destination _destination = null;
    protected MessageProducer _producer = null;
    protected ConcurrentHashMap<Integer, MessageConsumer> _consumer_pool = null;
    protected static Object lock = new Object();

    @PostConstruct
    public boolean init() {
        boolean initState = mainServer.conf().getSwitchOnActivemq();
        if (initState) {
            synchronized (lock) {
                try {
                    if (_connection == null) {
                        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(mainServer.conf()
                                                                                                              .getActivemqUser(),
                                                                                                    mainServer.conf()
                                                                                                              .getActivemqPwd(),
                                                                                                    mainServer.conf()
                                                                                                              .getActivemqUrl());
                        _connection = connectionFactory.createConnection();
                        _connection.start();
                    }
                    if (_destination == null) {
                        switch (getRuningStrategy()) {
                        case "all":
                            initState = initState && initProducer();
                            initState = initState && initConsumerPool();
                            break;
                        case "producer":
                            initState = initState && initProducer();
                            break;
                        case "consumer":
                            initState = initState && initConsumerPool();
                            break;
                        case "none": // 如果需要稍后自己手动构造的话
                        default:
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error("activemq初始化失败 init error", e);
                    mainServer.shutdown(-1);
                    initState = false;
                }
            }
        }
        return initState;
    }

    public boolean initProducer() throws JMSException {
        _session_producer = _connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        if (isQueue()) {
            _destination = _session_producer.createQueue(getQueueName());
        } else {
            _destination = _session_producer.createTopic(getQueueName());
        }
        _producer = _session_producer.createProducer(_destination);
        _producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        return true;
    }

    /**
     * 对于发到activemq的数据，要求必须是byte数组
     */
    public void sendMessage(byte[] value) {
        if (value != null) {
            try {
                BytesMessage message = _session_producer.createBytesMessage();
                message.writeBytes(value);
                _producer.send(message);
                _session_producer.commit();
            } catch (Exception e) {
                logger.error("activemq 数据发送失败", e);
            }
        }
    }

    public boolean initConsumerPool() throws JMSException {
        _session_consumer = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        if (isQueue()) {
            _destination = _session_consumer.createQueue(getQueueName());
        } else {
            _destination = _session_consumer.createTopic(getQueueName());
        }
        _consumer_pool = new ConcurrentHashMap<>();
        for (int i = 1; i < mainServer.conf().activemqListeningConsumers + 1; i++) {
            MessageConsumer consumer = null;
            if (mainServer.conf().getActivemqTopicIfDurable() && !isQueue()) {
                logger.info("activemq consumer[" + i + "] 初始化为 Topic");
                consumer = _session_consumer.createDurableSubscriber((Topic) _destination,
                                                                     getTopicDurableName());
            } else {
                logger.info("activemq consumer[" + i + "] 初始化为 Queue.");
                consumer = _session_consumer.createConsumer(_destination);
            }
            _consumer_pool.put(i, consumer);
        }
        return startConsumer();
    }

    public boolean startConsumer() throws JMSException {
        for (int i = 1; i < mainServer.conf().getActivemqListeningConsumers() + 1; i++) {
            logger.info("start activemq 消费者[" + i + "] ");
            _consumer_pool.get(i).setMessageListener(new ActiveMQListener(String.valueOf(i)));
        }
        return true;
    }

    public abstract boolean isQueue();

    public abstract boolean isTransacted();

    public abstract String getQueueName();

    public abstract String getTopicDurableName();

    public abstract String getRuningStrategy();

    /**
     * 如果启用consumer，必须实现这个方法
     */
    public boolean consumeMessage(byte[] data) {
        return true;
    }

    protected void genJob(byte[] data) throws NoSuchMethodException, SecurityException {
        // 借助内部阻塞队列隔离计算压力
        QueueJob job = new QueueJob(data,
                                    this.getClass().getMethod("consumeMessage", byte[].class),
                                    this);
        queueServer.addAJob(job);
    }

    @Data
    class ActiveMQListener implements MessageListener {
        protected String listenerId;

        public ActiveMQListener(String id) {
            this.listenerId = id;
        }

        @Override
        public void onMessage(Message message) {
            try {
                if (message instanceof BytesMessage) {
                    BytesMessage byteMessage = (BytesMessage) message;
                    byte[] data = new byte[(int) byteMessage.getBodyLength()];
                    byteMessage.readBytes(data);
                    genJob(data);
                }
            } catch (Exception e) {
                logger.info("解析到来的Message出错", e);
            }
        }
    }

    public void closeAll() {
        synchronized (lock) {
            try {
                if (_consumer_pool != null) {
                    for (int i = 1; i < mainServer.conf().getActivemqListeningConsumers()
                            + 1; i++) {
                        MessageConsumer consumer = _consumer_pool.get(i);
                        if (consumer != null) {
                            consumer.close();
                        }
                    }
                    _consumer_pool = null;
                }
                if (_producer != null) {
                    _producer.close();
                    _producer = null;
                }
                if (_session_producer != null) {
                    _session_producer.close();
                    _session_producer = null;
                }
                if (_session_consumer != null) {
                    _session_consumer.close();
                    _session_consumer = null;
                }
                if (_connection != null) {
                    _connection.close();
                    _connection = null;
                }
            } catch (JMSException e) {
                logger.error("activemq 关闭过程出错.", e);
            }
        }
    }
}

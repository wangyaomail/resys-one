package xmt.resys.common.dao.l2;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import xmt.resys.common.bean.job.QueueJob;
import xmt.resys.common.dao.l1.BaseKVDao;
import xmt.resys.common.server.QueueServer;
import xmt.resys.util.set.HBCollectionUtil;

/**
 * kafka不借助spring的kafka集成，因为后面有非spring的java运行kafka的场景
 * @info 系统只需要一个producer的出口，而每个topic对应一个consumer
 * @warn 不要用这个类实现对多个topic的读取，一个dao读一个topic
 */
public abstract class BaseKafkaDao<K, V> extends BaseKVDao<K, V> {
    @Autowired
    private QueueServer queueServer; // 保证性能，所有对象解析都放在这里执行
    protected KafkaProducer<K, V> _producer = null;
    protected KafkaConsumer<K, V> _consumer = null;
    protected AtomicBoolean isShutdown = new AtomicBoolean(false);
    private static Object lock = new Object();

    @PostConstruct
    public boolean init() {
        boolean initState = true;
        if (mainServer.conf().getSwitchOnKafka()) {
            synchronized (lock) {
                if (_producer == null && _consumer == null) {
                    switch (getTopicRuningStrategy()) {
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
            }
            if (!initState) {
                mainServer.shutdown(-1);
            }
        }
        return initState;
    }

    public abstract String getTopic();

    public abstract Integer getTopicPartition();

    public abstract String getTopicRuningStrategy(); // all=producer+consumer，或单独写这两个

    /**
     * topic的key的序列化模板，如果K不是String，这里一定要改动
     */
    public String getTopicKeySerializer() {
        return "org.apache.kafka.common.serialization.StringSerializer";
    }

    public String getTopicKeyDeserializer() {
        return "org.apache.kafka.common.serialization.StringDeserializer";
    }

    /**
     * topic的value的序列化模板，如果V不是String，这里一定要改动
     */
    public String getTopicValueSerializer() {
        return "org.apache.kafka.common.serialization.StringSerializer";
    }

    public String getTopicValueDeserializer() {
        return "org.apache.kafka.common.serialization.StringDeserializer";
    }

    /**
     * 对于每个consumer收到的数据做处理的地方
     */
    public abstract boolean jobFunction(ConsumerRecords<K, V> records);

    private boolean initProducer() {
        try {
            Properties properties = new Properties();
            properties.put("bootstrap.servers", mainServer.conf().getKafkaUrl());
            properties.put("key.serializer", getTopicKeySerializer());
            properties.put("value.serializer", getTopicValueSerializer());
            properties.put("request.required.acks", "1");
            properties.put("serializer.class", "kafka.serializer.DefaultEncoder");
            properties.put("linger.ms", "3");
            properties.put("retries", "0");
            properties.put("metadata.fetch.timeout.ms", "60000");
            properties.put("request.timeout.ms", "30000");
            properties.put("timeout.ms", "30000");
            _producer = new KafkaProducer<K, V>(properties);
            return true;
        } catch (Exception e) {
            logger.error("kafka producer init error!", e);
            return false;
        }
    }

    private boolean initConsumerPool() {
        try {
            Properties properties = new Properties();
            properties.put("bootstrap.servers", mainServer.conf().getKafkaUrl());
            properties.put("group.id", mainServer.conf().getKafkaGroupId());
            properties.put("enable.auto.commit", "true");
            properties.put("auto.commit.interval.ms", "1000");
            properties.put("key.deserializer", getTopicKeyDeserializer());
            properties.put("value.deserializer", getTopicValueDeserializer());
            properties.put("zookeeper.connect", mainServer.conf().getZookeeperConnector());
            properties.put("zookeeper.session.timeout.ms", "120000");
            properties.put("consumer.timeout.ms", "600000");
            properties.put("auto.commit.interval.ms", "1000");

            _consumer = new KafkaConsumer<>(properties);
            _consumer.subscribe(HBCollectionUtil.stringToList(getTopic()));
            while (true) {
                // consumer的时间间隔建议不要设置太小，它会一次性把这个时间间隔内的数据全部拿过来处理再返回，设置得太小slave调度压力就会略大
                ConsumerRecords<K, V> records = _consumer.poll(Duration.ofMillis(500));
                if (isShutdown.get()) { // 如果检测到关闭信号，则迅速退出
                    break;
                }
                if (!records.isEmpty()) {
                    QueueJob queueJob = new QueueJob(records,
                                                     this.getClass()
                                                         .getMethod("jobFunction",
                                                                    ConsumerRecords.class),
                                                     this);
                    queueServer.addAJob(queueJob);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("kafka producer init error!", e);
            return false;
        }
    }

    /**
     * 异步发送，不等待反馈
     */
    public boolean asyncSend(K key,
                             V value) {
        if (key != null && value != null && key.toString().length() > 0) {
            _producer.send(new ProducerRecord<>(getTopic(), key, value));
            return true;
        } else {
            return false;
        }
    }

    /**
     * 同步发送，每次发送都会等待反馈确认
     */
    public boolean send(K key,
                        V value) {
        if (key != null && value != null && key.toString().length() > 0) {
            Future<RecordMetadata> sendResult = _producer.send(new ProducerRecord<>(getTopic(),
                                                                                    key,
                                                                                    value));
            try {
                sendResult.get(10, TimeUnit.SECONDS); // 最多等10s
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 柔性关闭kafka连接
     */
    public void shutdown() {
        if (!isShutdown.getAndSet(true)) {
            synchronized (lock) {
                // 关闭producer
                if (_producer != null) {
                    _producer.close(Duration.ofMillis(3000));
                    _producer = null;
                }
                // 关闭consumer
                if (_consumer != null) {
                    _consumer.close(Duration.ofMillis(3000));
                    _producer = null;
                }
            }
        }
    }
}

package xmt.resys.resys.dao;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l2.BaseKafkaDao;

/**
 * 用户点击事件，都以json化的字符串存到kafka中
 */
@Repository("recUserListClickKafkaDao")
public class RecUserListClickKafkaDao extends BaseKafkaDao<String, String> {

    @Override
    public String getTopic() {
        return mainServer.conf().getKafkaUserListClickTopicName();
    }

    @Override
    public Integer getTopicPartition() {
        return mainServer.conf().getKafkaUserListClickTopicPartition();
    }

    @Override
    public String getTopicRuningStrategy() {
        return "producer";
    }

    /**
     * 因为只发送，所以不需要填充这个方法
     */
    @Override
    public boolean jobFunction(ConsumerRecords<String, String> records) {
        return false;
    }

}

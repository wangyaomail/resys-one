package xmt.resys.userlog;

import javax.annotation.Resource;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l2.BaseKafkaDao;
import xmt.resys.util.set.HBCollectionUtil;
import xmt.resys.util.set.HBStringUtil;

/**
 * 从kafka中接收用户的点击并转存HBase
 */
@Repository("userListClickKafkaReceiveDao")
public class UserListClickKafkaReceiveDao extends BaseKafkaDao<String, String> {
    @Resource
    private UserListClickHBaseDao userListClickHBaseDao;

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
        return "consumer";
    }

    /**
     * 从kafka中接收对象，因为经过json传递，所以两个类只要字段有相关的即可
     * @key resysid-clickid
     * @value UserListClickRecord json string
     * @hbasekey userid(以userid打头能够按照key直接进行scan检索):recid(这个等同于整个推荐过程的sessionid，并且第一时间在hbase中进行聚合)
     */
    @Override
    public boolean jobFunction(ConsumerRecords<String, String> records) {
        for (ConsumerRecord<String, String> record : records) {
            if (HBStringUtil.isNotBlank(record.value())) {
                logger.debug(record.value());
                UserListClickRecord ulcr = UserListClickRecord.fromJSONString(record.value());
                if (ulcr != null && ulcr.getRecid() != null) {
                    Put put = new Put(Bytes.toBytes(ulcr.getUserid() + ":" + ulcr.getRecid()));
                    if (ulcr.getList() != null) {
                        // 如果用户点击多次，list会覆盖式保存，注意要对hbase的历史版本数量进行限制
                        put.addColumn(Bytes.toBytes("data"),
                                      Bytes.toBytes("list"),
                                      Bytes.toBytes(HBCollectionUtil.listToString(ulcr.getList())));
                    }
                    // 保存在哪个时刻点击了哪个
                    if (ulcr.getClick() != null && ulcr.getClickTime() != null) {
                        put.addColumn(Bytes.toBytes("data"),
                                      Bytes.toBytes("click-" + ulcr.getClickTime()),
                                      Bytes.toBytes(ulcr.getClick()));
                    }
                    userListClickHBaseDao.put(put);
                }
            }
        }
        return false;
    }

}

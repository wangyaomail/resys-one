package xmt.resys.tag;

import javax.annotation.Resource;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;

import xmt.resys.common.dao.l2.BaseActiveMQDao;
import xmt.resys.util.set.HBStringUtil;

@Repository("tagChangeReceiveDao")
public class TagChangeReceiveDao extends BaseActiveMQDao {
    @Resource
    private TagHBaseDao tagHBaseDao;

    @Override
    public boolean isQueue() {
        return true;
    }

    @Override
    public boolean isTransacted() {
        return false;
    }

    @Override
    public String getQueueName() {
        return "tag-add";
    }

    @Override
    public String getTopicDurableName() {
        return "resys-tag-add-duration";
    }

    @Override
    public String getRuningStrategy() {
        return "consumer";
    }

    /**
     * 实际计算压力大的代码
     */
    @Override
    public boolean consumeMessage(byte[] data) {
        // 解析读入的对象
        try {
            JSONObject object = JSONObject.parseObject(new String(data, "UTF-8"));
            logger.info("拿到传来的tag的json" + object.toJSONString());
            // 解析传来的json
            String id = object.getString("id");
            if (HBStringUtil.isNotBlank(id)) {
                // 检查是否是删除
                Boolean delete = object.getBoolean("delete");
                if (delete != null) {
                    tagHBaseDao.remove(id);
                } else {
                    // 不是删除就是修改，添加也是修改
                    Put put = new Put(Bytes.toBytes(id));
                    tagHBaseDao.addColumn(put, "id", id);
                    tagHBaseDao.addColumn(put, "parent", object.getString("parent"));
                    tagHBaseDao.addColumn(put, "w", object.getString("w"));
                    tagHBaseDao.put(put);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("解析json对象出错", e);
            return false;
        }
    }

}

package xmt.resys.videobase;

import javax.annotation.Resource;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import xmt.resys.common.dao.l2.BaseActiveMQDao;
import xmt.resys.util.set.HBStringUtil;

@Repository("articleChangeReceiveDao")
public class ArticleChangeReceiveDao extends BaseActiveMQDao {
    @Resource
    private ArticleHBaseDao articleHBaseDao;

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
        return "videobase-add";
    }

    @Override
    public String getTopicDurableName() {
        return "resys-videobase-add-duration";
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
            logger.info("拿到传来的videobase的json" + object.toJSONString());
            // 解析传来的json
            String id = object.getString("id");
            if (HBStringUtil.isNotBlank(id)) {
                // 检查是否是删除
                Boolean delete = object.getBoolean("delete");
                if (delete != null) {
                    articleHBaseDao.remove(id);
                } else {
                    // 不是删除就是修改，添加也是修改
                    Put put = new Put(Bytes.toBytes(id));
                    articleHBaseDao.addColumn(put, "id", id);
                    articleHBaseDao.addColumn(put, "title", object.getString("title"));
                    articleHBaseDao.addColumn(put, "content", object.getString("content"));
                    articleHBaseDao.addColumn(put, "state", object.getInteger("state"));
                    articleHBaseDao.addColumn(put, "createTime", object.getDate("createTime"));
                    articleHBaseDao.addColumn(put, "updateTime", object.getDate("updateTime"));
                    articleHBaseDao.addColumn(put, "publishTime", object.getDate("publishTime"));
                    articleHBaseDao.addColumn(put, "source", object.getString("source"));
                    articleHBaseDao.addColumn(put,
                                              "articleAuthor",
                                              object.getString("articleAuthor"));
                    JSONArray array = object.getJSONArray("categorys");
                    if (array != null && array.size() > 0) {
                        articleHBaseDao.addColumn(put, "categorys", array.toJSONString());
                    }
                    array = object.getJSONArray("tags");
                    if (array != null && array.size() > 0) {
                        articleHBaseDao.addColumn(put, "tags", array.toJSONString());
                    }
                    articleHBaseDao.addColumn(put, "clickNum", object.getInteger("clickNum"));
                    articleHBaseDao.addColumn(put,
                                              "qualityScore",
                                              object.getDouble("qualityScore"));
                    articleHBaseDao.put(put);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("解析json对象出错", e);
            return false;
        }
    }

}

package xmt.resys.content.dao;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import xmt.resys.common.dao.l3.BaseMongoDao;
import xmt.resys.content.bean.mongo.HBArticle;
import xmt.resys.util.set.HBCollectionUtil;
import xmt.resys.util.set.HBStringUtil;

/**
 * 拦截对article做的所有增删改操作，向后发activemq的bean
 */
@Repository("articleDao")
public class ArticleDao extends BaseMongoDao<HBArticle> {

    @Resource
    private ArticleChangeSendDao articleChangeSendDao;

    @Override
    protected HBArticle postInsert(HBArticle bean) {
        if (bean != null && HBStringUtil.isNotBlank(bean.getId())) {
            JSONObject object = new JSONObject(bean.toMongoHashMap());
            articleChangeSendDao.sendMessage(object.toJSONString().getBytes());
        }
        return super.postInsert(bean);
    }

    @Override
    protected Collection<HBArticle> postInsert(Collection<HBArticle> beans) {
        if (HBCollectionUtil.isNotEmpty(beans)) {
            beans.forEach(bean -> postInsert(bean));
        }
        return super.postInsert(beans);
    }

    @Override
    protected void postUpdate(String id,
                              Map<String, Object> kvs,
                              UpdateResult result) {
        // 如果文章被修改，把新的文章数据向后发送
        if (result.getModifiedCount() > 0) {
            if (HBStringUtil.isNotBlank(id) && HBCollectionUtil.isNotEmpty(kvs)) {
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.putAll(kvs);
                articleChangeSendDao.sendMessage(object.toJSONString().getBytes());
            }
        }
        super.postUpdate(id, kvs, result);
    }

    @Override
    protected void postUpdate(Collection<String> ids,
                              Map<String, Object> kvs,
                              UpdateResult result) {
        if (result.getModifiedCount() > 0) {
            if (HBCollectionUtil.isNotEmpty(ids)) {
                ids.forEach(id -> postUpdate(id, kvs, result));
            }
        }
        super.postUpdate(ids, kvs, result);
    }

    @Override
    protected void postDelete(String id,
                              DeleteResult result) {
        if (result.getDeletedCount() > 0) {
            if (HBStringUtil.isNotBlank(id)) {
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("delete", true);
                articleChangeSendDao.sendMessage(object.toJSONString().getBytes());
            }
        }
        super.postDelete(id, result);
    }

    @Override
    protected void postDelete(Collection<String> ids,
                              DeleteResult result) {
        if (result.getDeletedCount() > 0) {
            if (HBCollectionUtil.isNotEmpty(ids)) {
                ids.forEach(id -> postDelete(id, result));
            }
        }
        super.postDelete(ids, result);
    }
}

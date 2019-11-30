package xmt.resys.common.service;

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import xmt.resys.common.bean.enums.ApiCode;
import xmt.resys.common.bean.http.ResponseBean;
import xmt.resys.common.bean.mongo.BaseIdBean;
import xmt.resys.common.bean.mongo.Page;
import xmt.resys.common.dao.l2.BaseCRUDDao;

/**
 * 对应对象增删改查的服务
 * @INCLUDE 是否使用缓存应该在dao层面实现，这里不做更多而封装
 */
public abstract class BaseCRUDService<T extends BaseIdBean> extends BaseService {
    // @WARN 提供无处不在的mongoTempate的调用，但考虑到dao的特性务必不要滥用
    @Autowired
    protected MongoTemplate mongoTemplate;

    /**
     * 无论是带缓存的dao还是不带缓存的dao，都请封装在dao层中而不要暴露给service
     * @WARN service不要再覆盖dao的直接访问，如果有需要访问请直接调用dao的访问解决
     */
    public abstract BaseCRUDDao<T> dao();

    public Class<T> getClassT() {
        return dao().getClassT();
    }

    public Object solveData(JSONObject data) {
        return solveData(data.getString("type"), data);
    }

    @SuppressWarnings("unchecked")
    public Object solveData(String type,
                            JSONObject data) {
        String method = data.getString("method");
        Map<String, Object> condition = null;
        if (data.containsKey("condition")) {
            condition = JSONObject.parseObject(data.getJSONObject("condition").toString(),
                                               new TypeReference<Map<String, Object>>() {});
        } else {
            if (!method.equals("count")) {
                return "{\"code\": \"" + ApiCode.PARAM_CONTENT_ERROR.getCode()
                        + "\",\"errMsg\": \"传递的字段不全\"}";
            }
        }
        ResponseBean responseBean = new ResponseBean();
        switch (method) {
        case "findOne":
            responseBean.setData(dao().findOne(condition));
            break;
        case "count":
            responseBean.setData(dao().count());
            break;
        case "query":
            Page<T> page = null;
            if (data.containsKey("page")) {
                page = data.getObject("page", Page.class);
            } else {
                page = new Page<T>();
                page.setPageNumber(1);
                page.setPageSize(10);
            }
            responseBean.setData(dao().getMongoDao().query(condition, page));
            break;
        case "queryAll":
            responseBean.setData(dao().findAll(condition));
            break;
        default:
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("参数传递错误，method：" + method + " 不存在");
        }
        return JSON.toJSONString(responseBean);
    }

    /**
     * 从mongo中查询数据对应的最大id号，如果表是空表，从0开始，只能用在有getId方法的对象上，否则会有问题
     */
    public String getMaxStringId() {
        Query query = new Query();
        query.with(Sort.by(new Order(Direction.DESC, "id")));
        query.limit(1);
        T data = mongoTemplate.findOne(query, dao().getClassT());
        try {
            return data == null ? "0" : (String) dao().getClassT().getMethod("getId").invoke(data);
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getMaxIntegerId() {
        String id = getMaxStringId();
        try {
            if (StringUtils.isNoneBlank(id)) {
                Integer maxId = Integer.parseInt(id);
                return maxId;
            }
        } catch (Exception e) {
            logger.error("parse num error!", e);
        }
        return null;
    }

    // sychonized
    // 这一部分是该服务的多线程运用部分，供一些用到多线程资源的场合使用
    protected ReentrantReadWriteLock serviceLock = new ReentrantReadWriteLock();
}

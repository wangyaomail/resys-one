package xmt.resys.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RestController;

import xmt.resys.common.bean.anno.RegisterMethod;
import xmt.resys.common.bean.enums.ApiCode;
import xmt.resys.common.bean.http.ResponseBean;
import xmt.resys.common.bean.mongo.BaseMgBean;
import xmt.resys.common.bean.mongo.Page;
import xmt.resys.common.service.BaseCRUDService;
import xmt.resys.util.set.HBCollectionUtil;
import xmt.resys.util.set.HBStringUtil;

/**
 * 标准restfull实现类
 */
@RestController
public abstract class BaseCRUDController<T extends BaseMgBean<T>> extends BaseController {
    protected abstract BaseCRUDService<T> getService();

    /**
     * 提供子类覆盖，在get前先做一些事情
     */
    protected String prepareGet(String id,
                                ResponseBean responseBean) {
        return id;
    }

    /**
     * 提供子类覆盖，在get后做一些事情
     */
    protected T postGet(T object,
                        ResponseBean responseBean) {
        return object;
    }

    /**
     * 用get查询的时候，id号必须传递，否则会有问题
     * @WARN 注意因为get方法名重复，所以需要显式在路径中写上get名称
     * @INFO 如果需要使用该方法，请在子类重写该方法并注明对父类方法的调用
     * @INFO @RequestMapping(value = "/get/{id}", method = { RequestMethod.GET })
     * @INFO public ResponseBean get(@PathVariable("id") String id)
     */
    public ResponseBean get(String id) {
        ResponseBean responseBean = getReturn();
        if (prepareForAll(responseBean)) {
            if (StringUtils.isEmpty(id)) {
                responseBean.setCodeAndErrMsg(ApiCode.PARAM_CONTENT_ERROR.getCode(), "ID号未传入");
            } else {
                id = prepareGet(id, responseBean);
                if (responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
                    T object = getService().dao().findOne(id);
                    postGet(object, responseBean);
                    if (responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
                        responseBean.setData(object);
                    }
                }
            }
        }
        return returnBean(responseBean);
    }

    /**
     * 提供子类覆盖，在insert前先做一些事情
     */
    protected T prepareInsert(T object,
                              ResponseBean responseBean) {
        object.prepareHBBean();
        return object;
    }

    /**
     * 提供子类覆盖，在insert后再做一些事情
     */
    protected T postInsert(T object,
                           ResponseBean responseBean) {
        return object;
    }

    /**
     * 插入数据
     * @INFO 如果需要使用该方法，请在子类重写该方法并注明对父类方法的调用
     * @INFO @RequestMapping(value = "", method = { RequestMethod.PUT })
     * @INFO public ResponseBean insert(@RequestBody T object)
     */
    public ResponseBean insert(T object) {
        ResponseBean responseBean = getReturn();
        if (prepareForAll(responseBean)) {
            if (object != null) {
                object = prepareInsert(object, responseBean); // 必须重新赋值，就可以在方法体内部对对象进行重新指定
                if (responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
                    try {
                        getService().dao().insert(object);
                    } catch (DuplicateKeyException e) {
                        responseBean.setCodeEnum(ApiCode.DUPLICATE_KEY_ERROR);
                        logger.error(ApiCode.DUPLICATE_KEY_ERROR.getName(), e);
                    }
                }
                postInsert(object, responseBean);
            }
        }
        return returnBean(responseBean);
    }

    /**
     * 提供子类覆盖，在insertAll前先做一些事情
     */
    protected List<T> prepareInsertAll(List<T> objects,
                                       ResponseBean responseBean) {
        ArrayList<T> result = new ArrayList<>();
        for (T object : objects) {
            result.add(postInsert(object, responseBean));
        }
        return result;
    }

    /**
     * 批量添加
     * @INFO 如果需要使用该方法，请在子类重写该方法并注明对父类方法的调用
     * @INFO @RequestMapping(value = "/addAll", method = { RequestMethod.PUT })
     * @INFO public ResponseBean insertAll(@RequestBody List<T> object)
     */
    public ResponseBean insertAll(List<T> object) {
        ResponseBean responseBean = super.getReturn();
        if (object != null) {
            object = prepareInsertAll(object, responseBean);
            if (responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
                try {
                    for (T item : object) {
                        item.prepareHBBean();
                    }
                    responseBean.setData(getService().dao().insertAll(object).size());
                } catch (DuplicateKeyException e) {
                    responseBean.setCodeEnum(ApiCode.DUPLICATE_KEY_ERROR);
                    logger.error(ApiCode.DUPLICATE_KEY_ERROR.getName(), e);
                }
            }
        }
        return returnBean(responseBean);
    }

    /**
     * 提供子类覆盖，在update前先做一些事情
     */
    protected T prepareUpdate(T object,
                              ResponseBean responseBean) {
        return object;
    }

    /**
     * 提供子类覆盖，在所有更新类操作后都要执行的
     */
    public void postUpdate(T object,
                           ResponseBean responseBean) {}

    /**
     * 修改update
     * @INFO 如果需要使用该方法，请在子类重写该方法并注明对父类方法的调用
     * @INFO @RequestMapping(value = "/update", method = { RequestMethod.POST })
     * @INFO public ResponseBean update(@RequestBody T object)
     */
    public ResponseBean update(T object) {
        ResponseBean responseBean = getReturn();
        if (prepareForAll(responseBean)) {
            object = prepareUpdate(object, responseBean);
            if (responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
                getService().dao().updateOne(object.getId(), object.toMongoHashMap());
                responseBean.setData("修改成功");
            }
            postUpdate(object, responseBean);
        }
        return returnBean(responseBean);
    }

    /**
     * 修改upsert
     * @INFO 如果需要使用该方法，请在子类重写该方法并注明对父类方法的调用
     * @INFO @RequestMapping(value = "/upsert", method = { RequestMethod.POST })
     * @INFO public ResponseBean upsert(@RequestBody T object)
     */
    public ResponseBean upsert(T object) {
        ResponseBean responseBean = getReturn();
        if (prepareForAll(responseBean)) {
            object = prepareUpdate(object, responseBean);
            if (responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
                getService().dao().updateOne(object.getId(), object.toMongoHashMap(), true);
            }
            postUpdate(object, responseBean);
        }
        return returnBean(responseBean);
    }

    /**
     * 提供子类覆盖，在remove前先做一些事情
     */
    protected String prepareRemove(String id,
                                   ResponseBean responseBean) {
        return id;
    }

    protected List<String> prepareRemoveAll(List<String> ids,
                                            ResponseBean responseBean) {
        ArrayList<String> rsList = new ArrayList<>();
        for (String id : ids) {
            rsList.add(prepareRemove(id, responseBean));
        }
        return rsList;
    }

    /**
     * 提供子类覆盖，在remove后执行操作
     */
    public void postRemove(String id,
                           ResponseBean responseBean) {}

    protected void postRemoveAll(List<String> ids,
                                 ResponseBean responseBean) {
        for (String id : ids) {
            postRemove(id, responseBean);
        }
    }

    /**
     * 删除remove
     * @INFO 如果需要使用该方法，请在子类重写该方法并注明对父类方法的调用
     * @INFO @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
     * @INFO public ResponseBean remove(@PathVariable("id") String id)
     */
    public ResponseBean remove(String id) {
        ResponseBean responseBean = getReturn();
        if (prepareForAll(responseBean)) {
            id = prepareRemove(id, responseBean);
            if (responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
                getService().dao().removeOne(id);
            }
            postRemove(id, responseBean);
        }
        return returnBean(responseBean);
    }

    /**
     * 全部删除removeAll
     * @INFO 如果需要使用该方法，请在子类重写该方法并注明对父类方法的调用
     * @INFO @RequestMapping(value = "/all", method = { RequestMethod.DELETE })
     * @INFO public ResponseBean removeAll(@RequestBody List<String> ids)
     */
    public ResponseBean removeAll(List<String> ids) {
        ResponseBean responseBean = getReturn();
        if (prepareForAll(responseBean)) {
            ids = prepareRemoveAll(ids, responseBean);
            if (responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
                getService().dao().removeAll(ids);
            }
            postRemoveAll(ids, responseBean);
        }
        return returnBean(responseBean);
    }

    /**
     * 提供子类覆盖，在query前先做一些事情
     */
    protected T prepareQuery(T object,
                             ResponseBean responseBean) {
        return object;
    }

    protected List<String> prepareQueryFields(T object,
                                              ResponseBean responseBean) {
        return BaseMgBean.generateFieldListToList(object.getClass());
    }

    /**
     * 查询
     * @INFO 如果需要使用该方法，请在子类重写该方法并注明对父类方法的调用
     * @INFO @RequestMapping(value = "/query", method = { RequestMethod.POST })
     * @INFO public ResponseBean query(@RequestBody T object)
     */
    public ResponseBean query(T object) {
        ResponseBean responseBean = getReturn();
        if (prepareForAll(responseBean)) {
            object = prepareQuery(object, responseBean);
            if (responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
                List<String> fields = prepareQueryFields(object, responseBean);
                responseBean.setData(getService().dao()
                                                 .getMongoDao()
                                                 .queryToFields(object.toMongoHashMap(),
                                                                object.getPage(),
                                                                getSortKey(object),
                                                                getDirection(object),
                                                                fields));
            }
        }
        return returnBean(responseBean);
    }

    /**
     * 提供子类覆盖，在count前先做一些事情
     */
    protected void prepareCount(ResponseBean responseBean) {}

    /**
     * 计数
     * @INFO 如果需要使用该方法，请在子类重写该方法并注明对父类方法的调用
     * @INFO @RequestMapping(value = "/count", method = { RequestMethod.GET })
     * @INFO public ResponseBean count()
     */
    public ResponseBean count() {
        ResponseBean responseBean = getReturn();
        if (prepareForAll(responseBean)) {
            prepareCount(responseBean);
            if (responseBean.getCode().equals(ApiCode.SUCCESS.getCode())) {
                responseBean.setData(getService().dao().count());
            }
        }
        return returnBean(responseBean);
    }

    protected boolean prepareForAll(ResponseBean responseBean) {
        return true;
    }

    /**
     * sortKey和direction的配置
     */
    protected String getSortKey(T object) {
        return StringUtils.isNotEmpty(object.getSortKey()) ? object.getSortKey() : "id";
    }

    protected Direction getDirection(T object) {
        return object.getAsc() != null && object.getAsc() ? Direction.ASC : Direction.DESC;
    }

    /**
     * 非映射型controller
     * @WARN 非映射型的接口可以和除了get方法之外的映射型的同时存在，但不建议这样用
     * @INFO 非映射型controller如果要用必须在子controller类头上显式声明
     */
    @RegisterMethod(name = "get", declare = true)
    public T get() {
        String id = request.getParameter("id");
        if (HBStringUtil.isNotBlank(id)) {
            return getService().dao().findOne(id);
        } else {
            return null;
        }
    }

    @RegisterMethod(name = "remove", declare = true)
    public String remove() {
        String id = request.getParameter("id");
        if (HBStringUtil.isNotBlank(id)) {
            getService().dao().removeOne(id);
            return "删除完成";
        } else {
            return "没有传来id号";
        }
    }

    @RegisterMethod(name = "removeAll", declare = true)
    public String removeAll() {
        String[] ids = request.getParameterValues("ids");
        if (ids != null && ids.length > 0) {
            getService().dao().removeAll(HBCollectionUtil.arrayToList(ids));
            return "删除完成";
        } else {
            return "没有传来id号";
        }
    }

    @RegisterMethod(name = "query", declare = true)
    public Object query() {
        Page<T> page = null;
        try { // 检查有没有分页
            page = new Page<>();
            page.setPageNumber(Integer.parseInt(request.getParameter("pageNumber")));
            page.setPageSize(Integer.parseInt(request.getParameter("pageSize")));
        } catch (Exception e) {
            page = null;
        }
        List<String> fields = BaseMgBean.generateFieldListToList(getService().getClassT());
        Map<String, Object> params = new HashMap<>();
        for (String field : fields) {
            String param = request.getParameter(field);
            if (HBStringUtil.isNotBlank(param)) {
                params.put(field, param);
            }
        }
        if (page != null) {
            return getService().dao().findAll(params, page);
        } else {
            return getService().dao().findAll(params);
        }
    }

    @RegisterMethod(name = "count", declare = true)
    public long manCount() {
        return getService().dao().count();
    }
}

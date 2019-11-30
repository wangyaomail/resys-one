package xmt.resys.web.controller;

import java.lang.reflect.ParameterizedType;

import javax.annotation.PostConstruct;

import org.springframework.web.bind.annotation.RestController;

import xmt.resys.common.bean.mongo.BaseMgBean;
import xmt.resys.common.dao.l2.BaseCRUDDao;
import xmt.resys.common.dao.l3.BaseMongoDao;
import xmt.resys.common.service.BaseCRUDService;
import xmt.resys.common.service.FullBaseCRUDService;

/**
 * 对于一些对象，可以直接封装获取的方式，不需要复杂化，从UserEditFController的方式演化而来
 * @warn 这个类很方便但是要慎用，每次继承这个controller就会生成两个spring不太好控制的bean
 */
@RestController
public abstract class BaseBeanCRUDController<T extends BaseMgBean<T>>
        extends BaseCRUDController<T> {
    protected BaseCRUDService<T> baseService;
    protected BaseCRUDDao<T> baseDao;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void fullAutoInit() {
        ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
        baseDao = new BaseMongoDao<T>(mainServer,
                                      mongoTemplate,
                                      (Class<T>) pt.getActualTypeArguments()[0]) {};
        FullBaseCRUDService<T> service = new FullBaseCRUDService<T>(mongoTemplate,
                                                                    mainServer,
                                                                    timerService,
                                                                    baseDao) {};
        baseService = service;
    }

    @Override
    protected BaseCRUDService<T> getService() {
        return baseService;
    }
}

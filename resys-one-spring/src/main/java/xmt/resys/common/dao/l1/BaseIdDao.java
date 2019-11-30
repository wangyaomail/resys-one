package xmt.resys.common.dao.l1;

import java.lang.reflect.ParameterizedType;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;

import xmt.resys.common.bean.mongo.BaseIdBean;
import xmt.resys.common.server.MainServer;

/**
 * 最简单的基于BaseIdBean对象的数据库
 */
@DependsOn("sysConfService")
public abstract class BaseIdDao<T extends BaseIdBean> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected MainServer mainServer;

    public BaseIdDao() {}

    /**
     * 注意，如果使用new进行对象创建，mainServer必须手动指定
     */
    public BaseIdDao(MainServer mainServer) {
        this.mainServer = mainServer;
    }

    public Class<T> classT;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void postConstruct() {
        if (classT == null) {
            synchronized (this) {
                ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
                classT = (Class<T>) pt.getActualTypeArguments()[0];
                logger.info("对象【" + classT.getSimpleName() + "】的dao已注册");
            }
        }
    }

    public Class<T> getClassT() {
        return classT;
    }

    public abstract T findOne(String id);

    public abstract T insert(T object);

    public abstract boolean removeOne(String id);
}

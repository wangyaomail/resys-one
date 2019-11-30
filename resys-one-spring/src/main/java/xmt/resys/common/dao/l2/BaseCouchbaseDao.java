package xmt.resys.common.dao.l2;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;

import xmt.resys.common.bean.couchbase.BaseCouchbaseBean;
import xmt.resys.common.dao.l1.BaseIdDao;
import xmt.resys.util.set.HBStringUtil;

/**
 * 不建议直接把Recommendation存入couchbase，couchbase和外界交互的方式尽量以序列化的json为主
 * @WARN 注意分桶资源比较昂贵，那么我们需要更小心地处理查询到couchbase中的id，必须把id的前缀从数据层面隔离开，仅仅在到层面实现封装
 * @INFO cluster和bucket都是静态的，意味着系统初始化的时候只需要初始化一个即可，且bucket共用，通过前缀区分
 */
public abstract class BaseCouchbaseDao<T extends BaseCouchbaseBean<T>> extends BaseIdDao<T> {
    protected static CouchbaseCluster _cluster = null;
    protected static Bucket _bucket = null;
    private static Object lock = new Object();

    @PostConstruct
    public boolean init() {
        if (mainServer.conf().getSwitchOnCouchbase()) {
            synchronized (lock) {
                String[] serverUrls = mainServer.conf().getCouchbaseUrl().split(",");
                for (int i = 0; i < serverUrls.length; i++) {
                    logger.info("couchbase服务器【" + i + "】连接地址：" + serverUrls[i]);
                }
                try {
                    if (_cluster == null) {
                        _cluster = CouchbaseCluster.create(serverUrls);
                        _cluster.authenticate(mainServer.conf().getCouchbaseUsername(),
                                              mainServer.conf().getCouchbasePassword());
                    }
                    if (_bucket == null) {
                        _bucket = _cluster.openBucket(mainServer.conf().getCouchbaseBkt());
                    }
                    return true;
                } catch (Exception e) {
                    logger.error("couchbase初始化失败 init error", e);
                    mainServer.shutdown(-1);
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    /**
     * 数据的前缀
     */
    public abstract String getPrefix();

    /**
     * 数据的过期时间
     */
    public abstract Integer getTTL();

    @Override
    public Class<T> getClassT() {
        return super.getClassT();
    }

    @Override
    public T findOne(String id) {
        if (HBStringUtil.isNotBlank(id)) {
            try {
                T pac = getClassT().newInstance();
                return pac.fromDoc(_bucket.get(getPrefix() + id), getPrefix());
            } catch (Exception e) {
                logger.warn("从couchbase查找key=" + id + "失败", e);
            }
        }
        return null;
    }

    @Override
    public T insert(T object) {
        try {
            _bucket.upsert(object.toJsonDocument(getPrefix()), getTTL(), TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("向couchbase插入object=" + object + "失败", e);
        }
        return object;
    }

    @Override
    public boolean removeOne(String id) {
        try {
            if (HBStringUtil.isNotBlank(id)) {
                _bucket.remove(getPrefix() + id);
                return true;
            }
        } catch (Exception e) {
            logger.warn("从couchbase删除key=" + id + "失败", e);
        }
        return false;
    }
}

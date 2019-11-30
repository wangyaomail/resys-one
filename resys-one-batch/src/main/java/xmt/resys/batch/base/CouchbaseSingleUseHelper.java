package xmt.resys.batch.base;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;

import xmt.resys.util.http.DaoUtil;
import xmt.resys.util.set.HBStringUtil;

/**
 * 一次性Couchbase连接器，简化使用
 */
public class CouchbaseSingleUseHelper implements AutoCloseable {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected CouchbaseCluster _cluster = null;
    protected Bucket _bucket = null;
    protected int _ttl = 3 * 24; // 默认是3天

    public CouchbaseSingleUseHelper(String urls,
                                    String bucket,
                                    String username,
                                    String password,
                                    int ttl) {
        String[] serverUrls = urls.split(",");
        for (int i = 0; i < serverUrls.length; i++) {
            logger.info("couchbase服务器【" + i + "】连接地址：" + serverUrls[i]);
        }
        try {
            _cluster = CouchbaseCluster.create(serverUrls);
            _cluster.authenticate(username, password);
            _bucket = _cluster.openBucket(bucket);
            _ttl = ttl;
        } catch (Exception e) {
            logger.error("couchbase初始化失败 init error", e);
            System.exit(-1);
        }
    }

    @Override
    public void close() {
        DaoUtil.closeIfValid(_bucket);
    }

    public JsonDocument findOne(String id) {
        if (HBStringUtil.isNotBlank(id)) {
            try {
                return _bucket.get(id);
            } catch (Exception e) {
                logger.warn("从couchbase查找key=" + id + "失败", e);
            }
        }
        return null;
    }

    public JsonDocument insert(JsonDocument object) {
        try {
            _bucket.upsert(object, _ttl, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.warn("向couchbase插入object=" + object + "失败", e);
        }
        return object;
    }

    public boolean removeOne(String id) {
        try {
            if (HBStringUtil.isNotBlank(id)) {
                _bucket.remove(id);
                return true;
            }
        } catch (Exception e) {
            logger.warn("从couchbase删除key=" + id + "失败", e);
        }
        return false;
    }
}

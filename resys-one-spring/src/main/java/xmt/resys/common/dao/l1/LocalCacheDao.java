package xmt.resys.common.dao.l1;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 网站通用内存缓存，比起简单实现的hashmap多了数据的过期回收
 */
@Repository("stringLocalCacheDao")
public class LocalCacheDao {
    protected Cache<String, Object> localCache;

    @PostConstruct
    public void init() {
        localCache = CacheBuilder.newBuilder()
                                 .initialCapacity(100) // 缓存容器的初始容量大小（单位：个）
                                 .maximumSize(100000) // 最大容量大小（单位：个）
                                 .concurrencyLevel(8) // 并发级别
                                 .expireAfterAccess(3600, TimeUnit.SECONDS)
                                 .build(); // 数据的过期时间
    }

    public Object get(String key) {
        return (Object) localCache.getIfPresent(key);
    }

    public Object put(String key,
                      Object value) {
        localCache.put(key, value);
        return value;
    }

    public void remove(String key) {
        localCache.invalidate(key);
    }

}

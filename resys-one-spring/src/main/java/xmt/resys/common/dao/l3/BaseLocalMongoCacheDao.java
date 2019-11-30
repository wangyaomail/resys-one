package xmt.resys.common.dao.l3;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import xmt.resys.common.bean.http.HBCacheStats;
import xmt.resys.common.bean.mongo.BaseMgBean;
import xmt.resys.common.dao.l2.BaseCRUDDao;
import xmt.resys.util.set.HBCollectionUtil;

/**
 * 对Mongo使用本地缓存的dao，基于内存缓存
 * @INCLUDE 同时整合TreeMap之前的任务接口
 * @INCLUDE 增加缓存的二级索引机制
 */
@Getter
public abstract class BaseLocalMongoCacheDao<T extends BaseMgBean<T>> extends BaseCRUDDao<T> {
    @Resource
    protected MongoTemplate mongoTemplate;
    protected BaseMongoDao<T> baseMongoDao;
    protected LoadingCache<String, T> localCache;
    protected MyDataLoader dataLoader;
    protected MyRemovalListener removalListener;

    /**
     * 提供子类覆盖，是否数据从缓存中退出的时候会自动保存到数据库
     */
    public boolean autoSaveBeforeRemoval() {
        return true;
    }

    /**
     * 是否要在启动的时候加载所有的数据，默认不加载，如果加载，请修改getExpireAfterAccessBySecond函数设置过期时间为0
     */
    public boolean loadFullDataWhenInit() {
        return false;
    }

    /**
     * 提供子类覆盖，缓存容器的初始容量大小（单位：个）
     */
    public int getInitialCapacity() {
        return 100;
    }

    /**
     * 提供子类覆盖，最大容量大小（单位：个）
     */
    public int getMaximumSize() {
        return 10000;
    }

    /**
     * 提供子类覆盖，并发级别，最高同时能有几个线程提供服务
     */
    public int getConcurrencyLevel() {
        return 8;
    }

    /**
     * 提供子类覆盖，数据的过期时间，（单位：秒）
     */
    public int getExpireAfterAccessBySecond() {
        return 3600;
    }

    /**
     * 需要索引的对象的其它字段，这些字段保存的key的命名规则是：key+.+实际值
     * @INFO 如果需要用，请覆盖这个字段填入自己需要的值
     */
    public Collection<String> secondaryIndex = new HashSet<>();
    public String secondarySplitToken = ".";

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        Class<T> myClassT = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Document doc = myClassT.getAnnotation(Document.class);
        if (doc != null) {
            baseMongoDao = new BaseMongoDao<T>(mainServer, mongoTemplate, myClassT) {};
            dataLoader = new MyDataLoader();
            removalListener = new MyRemovalListener();
            localCache = CacheBuilder.newBuilder()
                                     .initialCapacity(getInitialCapacity()) // 缓存容器的初始容量大小（单位：个）
                                     .maximumSize(getMaximumSize()) // 最大容量大小（单位：个）
                                     .recordStats() // 记录缓存命中率
                                     .concurrencyLevel(getConcurrencyLevel()) // 并发级别
                                     .expireAfterAccess(getExpireAfterAccessBySecond(),
                                                        TimeUnit.SECONDS) // 数据的过期时间
                                     .removalListener(removalListener)
                                     .build(dataLoader);
            // 初始化后，加载数据库中的某个条件的数据
            if (loadFullDataWhenInit()) {
                localCache.putAll(baseMongoDao.findAll()
                                              .stream()
                                              .collect(Collectors.toMap(T::getId,
                                                                        Function.identity())));
            }
        } else {
            logger.error("FATAL！加载的数据没有Document注解");
            System.exit(-1);
        }
    }

    private class MyRemovalListener implements RemovalListener<String, T> {
        /**
         * 如果缓存中的对象失效，自动写入数据库
         * @FIXME 对于删除类操作，需要删除，还进行invalid的话，不太合适，但这里需要传进来信息，但guava没有这个方法，需要想办法解决这个问题
         */
        @Override
        public void onRemoval(RemovalNotification<String, T> notification) {
            // 数据从缓存移除的时候，自动刷写到mongo数据库中
            if (autoSaveBeforeRemoval() && notification.getValue() != null
                    && notification.getValue().isAutoSave()) {
                // 判断删掉的是否是二级索引，如果删掉的是二级索引的key那么不进行对数据库的操作
                if (!secondaryIndex.contains(notification.getKey().split("\\.")[0])) {
                    baseMongoDao.insert(notification.getValue(), true);
                }
            }
        }
    }

    private class MyDataLoader extends CacheLoader<String, T> {
        @Override
        public T load(String key) throws Exception {
            return baseMongoDao.findOne(key);
        }

        @Override
        public Map<String, T> loadAll(Iterable<? extends String> keys) throws Exception {
            Map<String, T> resultMap = new HashMap<>();
            for (String key : keys) {
                resultMap.put(key, null);
            }
            Collection<T> datas = baseMongoDao.findAll(resultMap.keySet());
            if (CollectionUtils.isNotEmpty(datas)) {
                datas.forEach(d -> {
                    resultMap.put(d.getId(), d);
                });
            }
            return resultMap;
        }
    }

    /**
     * 覆盖这个方法是为了能够同时删掉所有二级索引的key
     */
    public void invalidate(String id) {
        localCache.invalidate(id);
        secondaryIndex.forEach(t -> localCache.invalidate(t + secondarySplitToken + id));
    }

    public void invalidateAll(Collection<String> idlist) {
        localCache.invalidate(idlist);
        secondaryIndex.forEach(t -> idlist.forEach(id -> localCache.invalidate(t
                + secondarySplitToken + id)));
    }

    /**
     * 将当前缓存中所有数据写回数据库
     */
    public void persistNow() {
        localCache.invalidateAll();
    }

    /**
     * 缓存的插入操作需要首先清空缓存内当前存在id的对象，然后全部直接插入到数据库中
     */
    @Override
    public T insert(T object,
                    boolean override) {
        invalidate(object.getId());
        return baseMongoDao.insert(object, override);
    }

    /**
     * 缓存的插入操作需要首先清空缓存内当前存在id的对象，然后全部直接插入到数据库中
     */
    @Override
    public Collection<T> insertAll(Collection<T> objects,
                                   boolean override) {
        if (HBCollectionUtil.isNotEmpty(objects)) {
            invalidateAll(objects.stream().map(o -> o.getId()).collect(Collectors.toList()));
            return baseMongoDao.insertAll(objects, override);
        }
        return null;
    }

    /**
     * 按照id号查找，先查询缓存
     */
    public T findOne(String id) {
        try {
            T obj = localCache.get(id);
            obj.increaseVisit();
            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 按照查询条件的查找，直接走数据库，然后填入缓存
     */
    @Override
    public T findOne(Map<String, Object> params,
                     String... fields) {
        T object = baseMongoDao.findOne(params, fields);
        if (object != null) {
            object.increaseVisit();
            localCache.put(object.getId(), object);
        }
        return object;
    }

    /**
     * 这类列表访问就直接走数据库就行，不需要添加缓存，且也不需要更新访问量
     * @WARN 如果获取其中一个的时候出错，那么会报错且整个都不会返回
     * @deprecated 如果你不清楚调用这个方法的潜在问题，请用findAllOneByOne更安全
     */
    @Override
    public Collection<T> findAll(Collection<String> idlist,
                                 Map<String, Object> params,
                                 String... fields) {
        return baseMongoDao.findAll(idlist, params, fields);
    }

    @Override
    public boolean updateOne(String id,
                             Map<String, Object> kvs,
                             boolean insert) {
        invalidate(id);
        return baseMongoDao.updateOne(id, kvs, insert);
    }

    @Override
    public boolean updateAll(Collection<String> idlist,
                             Map<String, Object> kvs,
                             boolean insert) {
        invalidateAll(idlist);
        return baseMongoDao.updateAll(idlist, kvs, insert);
    }

    /**
     * 删除数据的话，删除缓存中的数据并从数据库清空
     */
    @Override
    public boolean removeOne(String id) {
        T object = localCache.getIfPresent(id);
        if (object != null) {
            // 如果缓存中有，那么从缓存清除且不需要更新到数据库中
            object.setAutoSave(false);
            invalidate(id);
        }
        return baseMongoDao.removeOne(id);
    }

    /**
     * 删除数据的话，删除缓存中的数据并从数据库清空
     */
    @Override
    public boolean removeAll(Collection<String> idlist) {
        ImmutableMap<String, T> objects = localCache.getAllPresent(idlist);
        if (HBCollectionUtil.isNotEmpty(objects)) {
            objects.values().forEach(o -> o.setAutoSave(false));
            invalidateAll(idlist);
        }
        return baseMongoDao.removeAll(idlist);
    }

    /**
     * 如果是按条件删除，需要先把这些待删除的数据都先查询出来
     * @WARN 这样做的效率会很低，而且一不小心服务器就挂了，所以务必务必尽量不要这样做
     */
    @Override
    public boolean removeAll(Map<String, Object> params) {
        Collection<T> objects = baseMongoDao.findAll(params, "_id");
        if (HBCollectionUtil.isNotEmpty(objects)) {
            invalidateAll(objects.stream().map(o -> o.getId()).collect(Collectors.toList()));
        }
        return baseMongoDao.removeAll(params);
    }

    @Override
    public List<T> query(Map<String, Object> params,
                         String sortKey) {
        return baseMongoDao.query(params, sortKey);
    }

    @Override
    public Class<T> getClassT() {
        return baseMongoDao.getClassT();
    }

    @Override
    public long count() {
        return baseMongoDao.count();
    }

    @Override
    public long count(Map<String, Object> params) {
        return baseMongoDao.count(params);
    }

    /**
     * 一个个去拿，解决getall返回错误的情况
     */
    public Collection<T> getAllOneByOne(Collection<String> keys) {
        ArrayList<T> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(keys)) {
            keys = keys.stream().filter(k -> k != null).collect(Collectors.toList());
            for (String key : keys) {
                try {
                    T obj = localCache.get(key);
                    if (obj != null) {
                        obj.increaseVisit();
                        result.add(obj);
                    }
                } catch (Exception e) {
                }
            }
        }
        return result;
    }

    /**
     * 只返回当前缓存内有的
     */
    public Collection<T> getAllPresent(Collection<String> keys) {
        if (CollectionUtils.isNotEmpty(keys)) {
            try {
                // 这里必须过滤值为null的，否则会出错，什么都不返回
                keys = keys.stream().filter(k -> k != null).collect(Collectors.toList());
                ImmutableMap<String, T> map = localCache.getAllPresent(keys); // 这样加载，如果有key对应的值不存在，也不会报错，但是不会向数据库加载
                if (HBCollectionUtil.isNotEmpty(map)) {
                    return map.values().stream().map(t -> {
                        t.increaseVisit();
                        return t;
                    }).collect(Collectors.toList());
                } else {
                    return null;
                }
            } catch (Exception e) {
                logger.debug("getall遇到错误的key" + e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 只返回当前缓存内有的，且一个个加载
     */
    public Collection<T> getAllPresentOneByOne(Collection<String> keys) {
        ArrayList<T> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(keys)) {
            keys = keys.stream().filter(k -> k != null).collect(Collectors.toList());
            for (String key : keys) {
                try {
                    T obj = localCache.getIfPresent(key);
                    if (obj != null) {
                        obj.increaseVisit();
                        result.add(obj);
                    }
                } catch (Exception e) {
                }
            }
        }
        return result;
    }

    public HBCacheStats getPrettyStats() {
        return HBCacheStats.fromStats(localCache.stats());
    }

    @Override
    public BaseMongoDao<T> getMongoDao() {
        return baseMongoDao;
    }
}

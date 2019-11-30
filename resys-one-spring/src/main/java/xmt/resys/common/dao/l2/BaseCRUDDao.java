package xmt.resys.common.dao.l2;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xmt.resys.common.bean.mongo.BaseIdBean;
import xmt.resys.common.dao.l1.BaseIdDao;
import xmt.resys.common.dao.l3.BaseMongoDao;
import xmt.resys.common.server.MainServer;
import xmt.resys.util.set.HBCollectionUtil;
import xmt.resys.util.set.HBStringUtil;

/**
 * 这个作为基础的dao
 * @WARN 因为本系统建立在NOSQL基础上，所以对于所有出现对象的场合，必须有一个ID号，设计DAO的时候ID号贯穿始终
 * @INFO 因为@Repository注解是按照名称进行注入的，所以Dao层依然保留接口
 * @INFO 这个接口是dao层最基础的接口，只提供最基础的增删改查的服务，dao层实现的所有高级组件都可以以这个接口的对象为基础实现
 * @INFO 这个接口不区分是否是缓存还是不是
 */
public abstract class BaseCRUDDao<T extends BaseIdBean> extends BaseIdDao<T> {

    public BaseCRUDDao() {}

    /**
     * 注意，如果使用new进行对象创建，mainServer必须手动指定
     */
    public BaseCRUDDao(MainServer mainServer) {
        super(mainServer);
    }

    /**
     * 新增
     * @param object   待添加的对象
     * @param override 如果遇到重复id的对象，true:覆盖，false:不添加
     */
    public abstract T insert(T object,
                             boolean override);

    /**
     * 默认的添加是覆盖式的
     */
    public T insert(T object) {
        return insert(object, true);
    }

    /**
     * 批量添加
     */
    public abstract Collection<T> insertAll(Collection<T> objects,
                                            boolean override);

    public Collection<T> insertAll(Collection<T> objects) {
        return insertAll(objects, true);
    }

    /**
     * 按照id号进行查询
     */
    public abstract T findOne(String id);

    public T findOne(String id,
                     String... fields) {
        if (HBStringUtil.isNotBlank(id)) {
            return findOne(HBCollectionUtil.arrayToMap("_id", id), fields);
        } else {
            return null;
        }
    }

    /**
     * 只按照一个条件进行的查询，考虑到...的限制，这种查询没法再指定field
     * @INFO 先检查kvs的数量是否是偶数，如果不是那么直接返回空值
     */
    public T findOne(Object... kvs) {
        if (kvs != null && kvs.length % 2 == 0) {
            return findOne(HBCollectionUtil.arrayToMap(kvs));
        } else {
            return null;
        }
    }

    public T findOne(Map<String, Object> params) {
        return findOne(params, "");
    }

    /**
     * 带返回字段的个体查询
     * @param fields 如果fields是空字符串，那么返回对象所有的字段
     */
    public abstract T findOne(Map<String, Object> params,
                              String... fields);

    /**
     * 获取所有数据
     */
    public Collection<T> findAll() {
        return findAll(new HashMap<>());
    }

    /**
     * 按照某个条件查询库里所有的数据
     * @INFO 先检查kvs的数量是否是偶数，如果不是那么直接返回空值
     */
    public Collection<T> findAll(Object... kvs) {
        if (kvs != null && kvs.length % 2 == 0) {
            return findAll(HBCollectionUtil.arrayToMap(kvs));
        } else {
            return null;
        }
    }

    /**
     * 按照某个条件查询库里所有符合条件的数据
     */
    public Collection<T> findAll(Map<String, Object> params) {
        return findAll(null, params);
    }

    /**
     * 按照某个条件查询库里所有符合条件的数据
     */
    public Collection<T> findAll(Map<String, Object> params,
                                 String... fields) {
        return findAll(null, params, fields);
    }

    /**
     * 按照某个条件查询库里所有id号符合的数据
     */
    public Collection<T> findAll(Collection<String> idlist) {
        return findAll(idlist, new HashMap<>());
    }

    /**
     * 按照id序号查询，且只返回field内的数据
     */
    public Collection<T> findAll(Collection<String> idlist,
                                 String... fields) {
        return findAll(idlist, new HashMap<>(), fields);
    }

    /**
     * 按照某个条件查询库里所有id号符合的数据，且满足条件kvs
     */
    public Collection<T> findAll(Collection<String> idlist,
                                 Object... kvs) {
        if (kvs != null && kvs.length % 2 == 0) {
            return findAll(idlist, HBCollectionUtil.arrayToMap(kvs));
        } else {
            return findAll(idlist, new HashMap<>());
        }
    }

    /**
     * 按照某个条件查询库里所有id号符合的数据，且满足条件params
     */
    public Collection<T> findAll(Collection<String> idlist,
                                 Map<String, Object> params) {
        return findAll(idlist, params, "");
    }

    /**
     * 按照某个条件查询库里所有id号符合的数据，且满足条件params
     * @param fields 如果传入的fields是空字符串，那么返回对象的所有内容
     */
    public abstract Collection<T> findAll(Collection<String> idlist,
                                          Map<String, Object> params,
                                          String... fields);

    /**
     * 修改对象，只修改一个
     * @WARN 必须指定id
     * @INFO 如果被修改的对象不存在默认不插入
     */
    public boolean updateOne(String id,
                             Map<String, Object> kvs) {
        return updateOne(id, kvs, false);
    }

    /**
     * 更新一个对象，等同于覆盖式insert
     */
    public boolean updateOne(T object) {
        return insert(object, true) != null;
    }

    /**
     * 修改对象，只修改一个
     * @WARN 必须指定id
     * @param nullToInsert 如果是true，那么如果对象不存在则需要插入
     */
    public abstract boolean updateOne(String id,
                                      Map<String, Object> kvs,
                                      boolean insert);

    /**
     * 修改对象，修改所有
     */
    public boolean updateAll(Collection<String> idlist,
                             Map<String, Object> kvs) {
        return updateAll(idlist, kvs, false);
    }

    public abstract boolean updateAll(Collection<String> idlist,
                                      Map<String, Object> kvs,
                                      boolean insert);

    /**
     * 按照id号删除一个对象
     */
    public abstract boolean removeOne(String id);

    /**
     * 按照id号列表删除所有对象
     */
    public abstract boolean removeAll(Collection<String> idlist);

    /**
     * 按照条件删除对象
     * @INFO 因为对于mongo，删除操作没法控制数量，所以不添加单个的条件删除
     */
    public abstract boolean removeAll(Map<String, Object> params);

    /**
     * 根据参数查询，不翻页，等同于findall
     */
    public Collection<T> query(Map<String, Object> params) {
        return findAll(params);
    }

    /**
     * 根据参数查询,不翻页,指定排序key
     */
    public abstract List<T> query(Map<String, Object> params,
                                  String sortKey);


    /**
     * 统计数据条数
     */
    public abstract long count();

    /**
     * 统计数据条件条数
     */
    public abstract long count(Map<String, Object> params);

    /**
     * 绕过基层，获取下一层的MongoDao
     * @WARN 除非你确信绕过缓存是没问题的，否则尽量不要绕过最基层的Dao
     */
    public abstract BaseMongoDao<T> getMongoDao();
}

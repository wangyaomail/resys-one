package xmt.resys.common.dao.l4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import lombok.Getter;
import xmt.resys.common.bean.mongo.BaseTreeMgBean;
import xmt.resys.common.dao.l3.BaseLocalMongoCacheDao;
import xmt.resys.util.set.HBCollectionUtil;
import xmt.resys.util.set.HBStringUtil;

/**
 * 对于有树形层级依赖关系的dao，依赖LocalMongoCacheDao实现，在这里实现的默认是提及
 * @INCLUDE 同时整合TreeMap之前的任务接口
 */
@Getter
public abstract class BaseTreeLocalMongoCacheDao<T extends BaseTreeMgBean<T>>
        extends BaseLocalMongoCacheDao<T> {
    /**
     * 如果这个返回是false，那么意味着对象体积比较大，没法批量导入缓存中使用
     */
    public boolean isTinyBean() {
        return true;
    }

    /**
     * 如果是树形的依赖关系，那么不要用Dao默认的全量加载的方式，而是以树形的数据为准
     */
    @Override
    public boolean loadFullDataWhenInit() {
        return true;
    }

    /**
     * 获取Tree型数据需要的全部数据
     * @INFO 会加载数据库中本collection内的所有内容，类似于Lazy加载，如果是tiny的对象还会缓存所有数据
     */
    public Map<String, T> getAllTree() {
        // 如果缓存的所有数据和数据库的数据条数一样，那么直接返回缓存即可，否则从数据库加载所有数据
        // 大于等于是因为我们会默认有root节点但不保存到数据库下
        Map<String, T> treedMap = null; // 虽然初始化是空，但返回一定不是
        if (localCache.size() >= count()) {
            Collection<T> allData = baseMongoDao.findAll();
            if (HBCollectionUtil.isNotEmpty(allData)) {
                treedMap = generateFatherAndChildRelation(allData);
                if (isTinyBean()) { // 如果数据比较tiny，全部加载到库里就行了
                    localCache.putAll(treedMap);
                }
            }
        } else {
            treedMap = new HashMap<>(localCache.asMap());
        }
        return treedMap;
    }

    /**
     * 构造父子之间的关系
     */
    public Map<String, T> generateFatherAndChildRelation(Collection<T> dataList) {
        Map<String, T> treedMap = new HashMap<>(2);
        if (HBCollectionUtil.isNotEmpty(dataList)) {
            try {
                treedMap = dataList.stream()
                                   .collect(Collectors.toMap(T::getId, Function.identity()));
                T root = getClassT().newInstance();
                root.setId("root");
                treedMap.put("root", root);
                root.setAutoSave(false);
                for (T ac : dataList) {
                    if (HBStringUtil.isNotBlank(ac.getParent())) {
                        T parent = treedMap.get(ac.getParent());
                        if (parent != null) {
                            if (HBCollectionUtil.isEmpty(parent.getChildren())) {
                                parent.setChildren(new ArrayList<>(4));
                            }
                            parent.getChildren().add(ac);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        return treedMap;
    }

    /**
     * 调用这个方法，意味着这个collection对应的是一个逻辑性的配置
     * @INFO 如果是能够全量加载数据库的类型，直接加载，否则必须走数据库
     */
    public T getBeanInTree(String id) {
        try {
            if (localCache.size() >= count()) {
                return getAllTree().get(id);
            } else {
                return localCache.get(id);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 递归删除
     */
    public boolean recursiveDelete(String id,
                                   int level) {
        boolean result = false;
        if (HBStringUtil.isNotBlank(id)) {
            level = level < 1 ? 2 : level;
            Set<String> removeSet = new HashSet<>();
            removeSet.add(id);
            List<String> parentList = new ArrayList<>();
            parentList.add(id);
            while (level-- > 0) {
                List<T> findList = baseMongoDao.query(Query.query(Criteria.where("parent")
                                                                          .in(parentList)));
                if (CollectionUtils.isNotEmpty(findList)) {
                    parentList = findList.stream().map(i -> i.getId()).collect(Collectors.toList());
                    removeSet.addAll(parentList);
                    continue;
                } else {
                    break;
                }
            }
            localCache.invalidateAll(removeSet);
            baseMongoDao.removeAll(removeSet);
            result = true;
        }
        return result;
    }

    /**
     * 更新某个节点的id号
     */
    public boolean updateId(T obj) {
        if (HBStringUtil.isNotBlank(obj.getId()) && HBStringUtil.isNotBlank(obj.getOldId())) {
            // 1、检查新节点是否存在
            T node = findOne(obj.getId());
            if (node == null) {
                // 如果节点不存在，插入新节点
                insert(obj);
            }
            // 2、为了不破坏缓存，先查出所有新节点
            Collection<T> children = findAll(HBCollectionUtil.arrayToMap("parent", obj.getOldId()),
                                             "_id");
            if (HBCollectionUtil.isNotEmpty(children)) {
                // 3、如果这个节点有孩子节点，把parent是oldId的标签都切换到新id上
                updateAll(children.stream().map(c -> c.getId()).collect(Collectors.toList()),
                          HBCollectionUtil.arrayToMap("parent", obj.getId()),
                          false);
            }
            // 4、删掉旧节点
            removeOne(obj.getOldId());
            return true;
        } else {
            return false;
        }
    }
}

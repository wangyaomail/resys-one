package xmt.resys.common.dao.l3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import xmt.resys.common.bean.mongo.BaseIdBean;
import xmt.resys.common.bean.mongo.Page;
import xmt.resys.common.dao.l2.BaseCRUDDao;
import xmt.resys.common.server.MainServer;
import xmt.resys.util.set.HBCollectionUtil;
import xmt.resys.util.set.HBStringUtil;

/**
 * 基础mongo数据库调用，比basedao多一些方法
 */
public abstract class BaseMongoDao<T extends BaseIdBean> extends BaseCRUDDao<T> {
    @Resource
    protected MongoTemplate mongoTemplate;

    public BaseMongoDao() {}

    public BaseMongoDao(MainServer mainServer,
                        MongoTemplate mongoTemplate,
                        Class<T> classT) {
        super(mainServer);
        this.mongoTemplate = mongoTemplate;
        this.classT = classT;
        org.springframework.data.mongodb.core.mapping.Document doc = classT.getAnnotation(org.springframework.data.mongodb.core.mapping.Document.class);
        if (doc != null) {
            collectionName = doc.collection();
        } else {
            logger.error("所查询对象没有collection！");
        }
    }

    protected T postInsert(T object) {
        return object;
    }

    protected Collection<T> postInsert(Collection<T> objects) {
        return objects;
    }

    /**
     * @WARN 如果override是false，且遇到key重复则会报错
     */
    @Override
    public T insert(T object,
                    boolean override) {
        if (override) {
            return postInsert(mongoTemplate.save(object));
        } else {
            return postInsert(mongoTemplate.insert(object));
        }
    }

    @Override
    public Collection<T> insertAll(Collection<T> objects,
                                   boolean override) {
        if (HBCollectionUtil.isNotEmpty(objects)) {
            if (override) {
                return postInsert(objects.stream()
                                         .map(o -> mongoTemplate.save(o))
                                         .collect(Collectors.toList()));
            } else {
                return postInsert(mongoTemplate.insertAll(objects));
            }
        } else {
            return null;
        }
    }

    public T findOne(String id) {
        return findOne(id, "");
    }

    @Override
    public T findOne(Map<String, Object> params,
                     String... fields) {
        Document queryObject = new Document(params);
        Document fieldsObject = new Document();
        if (fields != null && HBStringUtil.isNotBlank(fields[0])) {
            for (String field : fields) {
                fieldsObject.put(field, true);
            }
        }
        return mongoTemplate.findOne(new BasicQuery(queryObject, fieldsObject),
                                     getClassT(),
                                     getCollectionName());
    }

    public Collection<T> findAll(Collection<String> idlist,
                                 Map<String, Object> params,
                                 String... fields) {
        Document fieldsObject = new Document();
        if (fields != null && fields.length > 0 && HBStringUtil.isNotBlank(fields[0])) {
            for (String field : fields) {
                fieldsObject.put(field, true);
            }
        }
        Query query = new BasicQuery(new Document(params), fieldsObject);
        if (HBCollectionUtil.isNotEmpty(idlist)) {
            query.addCriteria(Criteria.where("_id").in(idlist));
        }
        return mongoTemplate.find(query, getClassT());
    }

    /**
     * 供子类覆盖来使用
     */
    protected void preUpdate() {}

    /**
     * 供子类覆盖来使用
     * @param result
     */
    protected void postUpdate(String id,
                              Map<String, Object> kvs,
                              UpdateResult result) {}

    protected void postUpdate(Collection<String> ids,
                              Map<String, Object> kvs,
                              UpdateResult result) {}

    @Override
    public boolean updateOne(String id,
                             Map<String, Object> kvs,
                             boolean insert) {
        UpdateResult result = null;
        Update update = new Update();
        Iterator<Entry<String, Object>> itr = kvs.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, Object> entry = itr.next();
            update.set(entry.getKey(), entry.getValue());
        }
        this.preUpdate();
        if (!insert) {
            result = mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(id)),
                                               update,
                                               getClassT());
            return result.getModifiedCount() > 0;
        } else {
            result = mongoTemplate.upsert(new Query(Criteria.where("_id").is(id)),
                                          update,
                                          getClassT());
        }
        this.postUpdate(id, kvs, result);
        return result.getModifiedCount() > 0;
    }

    @Override
    public boolean updateAll(Collection<String> idlist,
                             Map<String, Object> kvs,
                             boolean insert) {
        UpdateResult result = null;
        Update update = new Update();
        Iterator<Entry<String, Object>> itr = kvs.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, Object> entry = itr.next();
            update.set(entry.getKey(), entry.getValue());
        }
        this.preUpdate();
        if (insert) {
            result = mongoTemplate.upsert(new Query(Criteria.where("_id").in(idlist)),
                                          update,
                                          getClassT());
        } else {
            result = mongoTemplate.updateMulti(new Query(Criteria.where("_id").in(idlist)),
                                               update,
                                               getClassT());
        }
        this.postUpdate(idlist, kvs, result);
        return result.getModifiedCount() > 0;
    }

    protected void postDelete(String ids,
                              DeleteResult result) {}

    protected void postDelete(Collection<String> ids,
                              DeleteResult result) {}

    @Override
    public boolean removeOne(String id) {
        this.preUpdate();
        DeleteResult result = mongoTemplate.remove(new Query(Criteria.where("_id").is(id)),
                                                   getClassT());
        this.postDelete(id, result);
        return result.getDeletedCount() > 0;
    }

    @Override
    public boolean removeAll(Collection<String> idlist) {
        this.preUpdate();
        DeleteResult result = mongoTemplate.remove(new Query(Criteria.where("_id").in(idlist)),
                                                   getClassT());
        this.postDelete(idlist, result);
        return result.getDeletedCount() > 0;
    }

    /**
     * 慎用这个方法，如果传入了空字符串会一下子删掉所有数据
     */
    @Override
    public boolean removeAll(Map<String, Object> params) {
        // 先查出所有的id
        Collection<T> beans = findAll(params, "_id");
        // 按照id删除
        if (HBCollectionUtil.isNotEmpty(beans)) {
            return removeAll(beans.stream().map(b -> b.getId()).collect(Collectors.toList()));
        } else {
            return false;
        }
    }

    @Override
    public List<T> query(Map<String, Object> params,
                         String sortKey) {
        sortKey = sortKey == null ? "id" : sortKey;
        Query query = getGeneralQuery(params);
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    if (entry.getValue().equals("exists")) {// 判断该字段是否存在
                        query.addCriteria(Criteria.where(entry.getKey()).exists(true));
                    } else if (entry.getValue().equals("notexist")) {
                        query.addCriteria(Criteria.where(entry.getKey()).exists(false));
                    } else {
                        query.addCriteria(Criteria.where(entry.getKey()).is(entry.getValue()));
                    }
                }
            }
        }
        // 设置排序
        query.with(Sort.by(new Order(Direction.DESC, sortKey)));
        return mongoTemplate.find(query, getClassT(), getCollectionName());
    }

    public List<T> query(Query query) {
        return mongoTemplate.find(query, getClassT());
    }

    /**
     * 分页的这些不需要让缓存dao等使用，直接连接到mongo就行了
     */
    public Page<T> query(Map<String, Object> params,
                         Page<T> page) {
        return query(params, page, "id");
    }

    public Page<T> query(Map<String, Object> params,
                         Page<T> page,
                         String sortKey) {
        Query query = getGeneralQuery(params, page, sortKey, Direction.DESC, null);
        List<T> list = mongoTemplate.find(query, getClassT(), getCollectionName());
        page.setList(list);
        return page;
    }

    public Page<T> queryToFields(Map<String, Object> params,
                                 Page<T> page,
                                 List<String> filedsNameList) {
        return queryToFields(params, page, "id", filedsNameList);
    }

    public Page<T> queryToFields(Map<String, Object> params,
                                 Page<T> page,
                                 String sortKey,
                                 List<String> filedsNameList) {
        Query query = getGeneralQuery(params, page, sortKey, Direction.DESC, filedsNameList);
        List<T> list = mongoTemplate.find(query, getClassT(), getCollectionName());
        page.setList(list);
        return page;
    }

    public Page<T> queryToFields(Map<String, Object> params,
                                 Page<T> page,
                                 String sortKey,
                                 Direction direction,
                                 List<String> filedsNameList) {
        Query query = getGeneralQuery(params, page, sortKey, direction, filedsNameList);
        List<T> list = mongoTemplate.find(query, getClassT(), getCollectionName());
        page.setList(list);
        return page;
    }

    public Page<T> queryToFieldsByPrefix(Map<String, Object> params,
                                         Page<T> page,
                                         String sortKey,
                                         Direction direction,
                                         List<String> filedsNameList,
                                         String prefix) {
        filedsNameList.remove(prefix);
        Query query = getGeneralQuery(params, null, sortKey, direction, filedsNameList);
        query.addCriteria(Criteria.where(prefix).regex("^" + params.get(prefix) + ".*"));
        List<T> list = mongoTemplate.find(query, getClassT(), getCollectionName());
        page.setList(list);
        return page;
    }

    public List<T> queryToFields(Map<String, Object> params,
                                 String sortKey,
                                 List<String> filedsNameList) {
        Query query = getGeneralQuery(params, null, sortKey, Direction.DESC, filedsNameList);
        return mongoTemplate.find(query, getClassT(), getCollectionName());
    }

    public Page<T> aggregationToFieldsByPrefix(Map<String, Object> params,
                                               Page<T> page,
                                               String sortKey,
                                               Direction direction,
                                               List<String> filedsNameList,
                                               String prefix,
                                               String aggretionKey) {
        String prefixValue = null;
        if (StringUtils.isNotEmpty(prefix)) {
            if (filedsNameList != null)
                filedsNameList.remove(prefix);
            if (params != null && params.containsKey(prefix)) {
                prefixValue = params.get(prefix).toString();
                params.remove(prefix);
            }
        }
        Aggregation agg = getGeneralAggregation(params,
                                                HBCollectionUtil.getMapSplit(prefix,
                                                                             "^" + prefixValue
                                                                                     + ".*"),
                                                page,
                                                sortKey,
                                                direction,
                                                filedsNameList,
                                                aggretionKey);
        AggregationResults<T> list = mongoTemplate.aggregate(agg, getClassT(), getClassT());
        // agg2用于分页信息的改变
        Aggregation agg2 = getGeneralAggregationCount(params,
                                                      HBCollectionUtil.getMapSplit(prefix,
                                                                                   "^" + prefixValue
                                                                                           + ".*"),
                                                      aggretionKey);
        AggregationResults<T> list2 = mongoTemplate.aggregate(agg2, getClassT(), getClassT());
        page.setTotalSize(list2.getMappedResults().size());
        page.setList(list.getMappedResults());
        return page;
    }

    /**
     * 处理group时使用流式操作，务必注意顺序
     */
    protected Aggregation getGeneralAggregation(Map<String, Object> params,
                                                Map<String, String> regex,
                                                Page<T> page,
                                                String sortKey,
                                                Direction direction,
                                                List<String> filedsNameList,
                                                String groupKey) {
        List<AggregationOperation> aggOptions = new ArrayList<>();
        { // 添加查询条件
            List<Criteria> criteriaList = paramsToCriteriasList(params);
            if (MapUtils.isNotEmpty(regex)) {
                for (Map.Entry<String, String> entry : regex.entrySet()) {
                    criteriaList.add(Criteria.where(entry.getKey()).regex(entry.getValue()));
                }
            }
            aggOptions.add(Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]))));
        }
        { // 分组
            if (StringUtils.isNotEmpty(groupKey)) {
                aggOptions.add(new GroupOperation(Aggregation.fields(groupKey)).first("$$ROOT")
                                                                               .as("data"));
            }
        }
        { // 排序
            sortKey = "data." + (sortKey == null ? "id" : sortKey);
            List<Order> orders = new ArrayList<Order>();
            orders.add(new Order(direction, sortKey));
            Sort sort = Sort.by(orders);
            aggOptions.add(Aggregation.sort(sort));
        }
        { // 分页
            if (page != null) {
                aggOptions.add(Aggregation.skip((long) (page.getPageNumber() > 0
                        ? (page.getPageNumber() - 1) * page.getPageSize()
                        : 0)));
                aggOptions.add(Aggregation.limit(page.getPageSize()));
            }
        }
        { // 添加返回field
            if (CollectionUtils.isNotEmpty(filedsNameList)) {
                filedsNameList = filedsNameList.stream()
                                               .map(str -> "data." + str)
                                               .collect(Collectors.toList());
                aggOptions.add(Aggregation.project(filedsNameList.toArray(new String[filedsNameList.size()])));
            }
        }
        Aggregation aggregation = Aggregation.newAggregation(aggOptions);
        return aggregation;
    }

    /**
     * 处理group时使用流式操作，务必注意顺序,用过求count的Aggregation
     */
    protected Aggregation getGeneralAggregationCount(Map<String, Object> params,
                                                     Map<String, String> regex,
                                                     String groupKey) {
        List<AggregationOperation> aggOptions = new ArrayList<>();
        { // 添加查询条件
            List<Criteria> criteriaList = paramsToCriteriasList(params);
            if (MapUtils.isNotEmpty(regex)) {
                for (Map.Entry<String, String> entry : regex.entrySet()) {
                    criteriaList.add(Criteria.where(entry.getKey()).regex(entry.getValue()));
                }
            }
            aggOptions.add(Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]))));
        }
        { // 分组
            if (StringUtils.isNotEmpty(groupKey)) {
                aggOptions.add(new GroupOperation(Aggregation.fields(groupKey)).first("$$ROOT")
                                                                               .as("data"));
            }
        }
        Aggregation aggregation = Aggregation.newAggregation(aggOptions);
        return aggregation;
    }

    /**
     * 将查询条件添加到查询语句中
     * @WARN 我们花费了很大代价才将所有查询条件Map变查询语句的逻辑汇总在这里，如果今后要有相同的需求不要单独写
     */
    public List<Criteria> paramsToCriteriasList(Map<String, Object> params) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (HBCollectionUtil.isNotEmpty(params)) {
            if (mainServer.conf().getRunOnDev()) {
                logger.debug("查询参数：" + params);
            }
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    switch (entry.getValue().getClass().getName()) {
                    case "java.util.List":
                    case "java.util.ArrayList":
                    case "java.util.LinkedList":
                        criteriaList.add(Criteria.where(entry.getKey())
                                                 .in((Collection<?>) entry.getValue()));
                        break;
                    case "java.lang.String":
                        if (entry.getValue().equals("exists")) {// 是否是判断存在的条件
                            criteriaList.add(Criteria.where(entry.getKey()).exists(true));
                        } else if (entry.getValue().equals("notexist")) {
                            criteriaList.add(Criteria.where(entry.getKey()).exists(false));
                        } else {
                            criteriaList.add(Criteria.where(entry.getKey()).is(entry.getValue()));
                        }
                        break;
                    default:
                        criteriaList.add(Criteria.where(entry.getKey()).is(entry.getValue()));
                        break;
                    }
                }
            }
        }
        return criteriaList;
    }

    /**
     * 利用查询条件生成直接的查询语句
     */
    public Query getGeneralQuery(Map<String, Object> params) {
        return getGeneralQuery(params, null);
    }

    public Query getGeneralQuery(Map<String, Object> params,
                                 List<String> filedsNameList) {
        List<Criteria> criteriaList = paramsToCriteriasList(params);
        Query query = null;
        if (filedsNameList != null) {
            Document fieldsObject = new Document();
            for (String field : filedsNameList) {
                fieldsObject.put(field, true);
            }
            query = new BasicQuery(new Document(), fieldsObject);
        } else {
            query = new Query();
        }
        if (criteriaList.size() > 0) { // GOO为空报错
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()])));
        }
        return query;
    }

    /**
     * 按照查询条件生成查询语句
     * @param params         查询条件
     * @param page           传入的分页
     * @param sortKey        按照哪个key进行排序
     * @param direction      如果排序，是正序还是逆序
     * @param filedsNameList 返回哪些field
     */
    public Query getGeneralQuery(Map<String, Object> params,
                                 Page<T> page,
                                 String sortKey,
                                 Direction direction,
                                 List<String> filedsNameList) {
        Query query = getGeneralQuery(params, filedsNameList);
        sortKey = sortKey == null ? "id" : sortKey;
        List<Order> orders = new ArrayList<Order>(); // 排序
        orders.add(new Order(direction, sortKey));
        Sort sort = Sort.by(orders);
        if (page != null) {
            page.setSort(sort);
            Long count = mongoTemplate.count(query, getClassT(), getCollectionName());
            page.setTotalSize(count.intValue());
            query.with(page);
        } else {
            query.with(sort);
        }
        return query;
    }

    public String collectionName;

    /**
     * @WARN 如果不是用的spring的自动注入，不要调用这个方法，而是构造的时候传入必要的参数
     * @WARN classT放在最底层的类里进行构造
     */
    @PostConstruct
    private BaseMongoDao<T> checkAndSet() {
        if (collectionName == null) {
            synchronized (this) {
                // ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
                // classT = (Class<T>) pt.getActualTypeArguments()[0];
                // logger.info("对象【" + classT.getSimpleName() + "】的dao已注册");
                org.springframework.data.mongodb.core.mapping.Document doc = classT.getAnnotation(org.springframework.data.mongodb.core.mapping.Document.class);
                if (doc != null) {
                    collectionName = doc.collection();
                } else {
                    logger.error("所查询对象没有collection！");
                }
            }
        }
        return this;
    }

    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public long count() {
        return mongoTemplate.count(new Query(), getClassT(), getCollectionName());
    }

    @Override
    public long count(Map<String, Object> params) {
        return mongoTemplate.count(new Query(), getClassT(), getCollectionName());
    }

    @Override
    public BaseMongoDao<T> getMongoDao() {
        return this;
    }
}

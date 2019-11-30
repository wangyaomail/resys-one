package xmt.resys.common.bean.mongo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.anno.FieldUsage;
import xmt.resys.common.bean.enums.ApiType;
import xmt.resys.util.id.IDUtil;

/**
 * 这个类用于这个类下的field转化为hashmap，这样在和关系型数据库进行交互的时候比较有利 而且这样搞，也个java带来了动态语言的特点
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseMgBean<T> implements BaseIdBean {
    protected static final Logger logger = LoggerFactory.getLogger(BaseMgBean.class);
    // 这个字段的作用是提供能用的getset方法，这样哪怕子类是private的也能覆盖这里的id
    protected String id;
    @Transient
    public Page<T> page;
    @Transient
    private Boolean asc; // 显示的时候，是否是正序显示
    @Transient
    private String sortKey; // 排序的key值应该用什么
    @Transient
    private boolean autoSave = true; // 在缓存的情况下，是否要自动保存数据库，默认需要
    @Transient
    private long timeout = 3l * 3600; // 默认每个被缓存的数据都只有3小时的保存时间
    @Indexed(sparse = true, background = true) // 如果没有vn字段的，我们可以暂时不用它的排序功能。如果有的，那么就按照这个字段进行排序
    private Long vn; // 访问数量，做成通用值，如果没查询到之类的都会置为0

    public void increaseVisit() {
        vn = vn == null ? 1 : vn + 1;
    }

    protected static final HashSet<String> blackSet = new HashSet<>();
    static {
        blackSet.add("serialVersionUID");
        blackSet.add("logger");
        blackSet.add("blackSet");
        blackSet.add("page");
    }

    private Field getThisOrSuperField(String key) throws Exception {
        Field f = null;
        try {
            f = this.getClass().getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            // 这种情况可能是中间有父类，但是我们只处理一层，如果隔了一层还有东西，那就单独处理
            f = this.getClass().getSuperclass().getDeclaredField(key);
        }
        return f;
    }

    public Map<String, String> toHashMap(String... keys) {
        Map<String, String> map = new HashMap<>();
        if (keys == null || keys.length == 0) {
            return map;
        }
        for (String key : keys) {
            try {
                Field f = getThisOrSuperField(key);
                if (f != null) {
                    f.setAccessible(true);
                    Object value = f.get(this);
                    if (value != null && value.toString().length() > 0) {
                        map.put(key, value.toString());
                    }
                }
            } catch (Exception e) {
                logger.error("key[" + key + "] get error.", e);
            }
        }
        return map;
    }

    public Map<String, String> toHashMap() {
        return toHashMap(generateFieldList());
    }

    public Map<String, Object> toMongoHashMap(String... keys) {
        Map<String, Object> map = new HashMap<>();
        if (keys == null || keys.length == 0) {
            return map;
        }
        for (String key : keys) {
            try {
                Field f = getThisOrSuperField(key);
                if (f != null) {
                    f.setAccessible(true);
                    Object value = f.get(this);
                    if (value != null && value.toString().length() > 0) {
                        map.put(key, value);
                    } else if ("---".equals(value)) {
                        map.put(key, null);
                    }
                }
            } catch (Exception e) {
                logger.error("key[" + key + "] get error.", e);
            }
        }
        return map;
    }

    public Map<String, Object> toMongoHashMap() {
        return toMongoHashMap(generateFieldList());
    }

    public static HashSet<String> blacklistGet() {
        return blackSet;
    }

    public String[] generateFieldList() {
        List<String> fieldArr = generateFieldListToList(this.getClass());
        return fieldArr.toArray(new String[fieldArr.size()]);
    }

    public static List<String> generateFieldListToList(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        Field[] superfields = clazz.getSuperclass().getDeclaredFields();
        ArrayList<String> fieldArr = null;
        // 如果和BaseBean之间有中间继承，就需要进行二次映射
        if (clazz.getSuperclass() != BaseMgBean.class) {
            fieldArr = new ArrayList<>(superfields.length + fields.length);
            addFieldToList(fieldArr, superfields);
        } else {
            fieldArr = new ArrayList<>(fields.length);
        }
        addFieldToList(fieldArr, fields);
        return fieldArr;
    }

    /**
     * 注意，将来的场景需要精确控制使用场景
     */
    @Deprecated
    private static void addFieldToList(ArrayList<String> fieldArr,
                                       Field[] fields) {
        addFieldToList(fieldArr, fields, ApiType.ALL);
    }

    private static void addFieldToList(ArrayList<String> fieldArr,
                                       Field[] fields,
                                       ApiType scene) {
        for (Field field : fields) {
            String name = field.getName();
            if (!blacklistGet().contains(name)) {
                // 如果是Transient的变量，不存数据库
                if (field.getAnnotation(org.springframework.data.annotation.Transient.class) == null) {
                    // 注意，使用Transient是旧的方式，
                    fieldArr.add(name);
                } else if (field.getAnnotation(FieldUsage.class) != null) {
                    FieldUsage anno = field.getAnnotation(FieldUsage.class);
                    switch (scene) {
                    case ALL:
                        if (anno.save() || anno.send()) {
                            fieldArr.add(name);
                        }
                        break;
                    case SAVE:
                        if (anno.save()) {
                            fieldArr.add(name);
                        }
                        break;
                    case SEND:
                        if (anno.send()) {
                            fieldArr.add(name);
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
        }
    }

    // 在有些插入场合，需要对bean进行一些初始化的任务，该方法提供子类进行继承
    // 注意这个方法内的实现应保证能够重复调用，不会因为信息覆盖产生问题（对任何一个字段进行set时都应该判断该字段是否有值）
    public void prepareHBBean() {}

    // 生成id号的规则默认是按照秒级的时间来，这个方法不要写到默认prepare方法中，否则会使得很多之前生成的id号都被新策略覆盖
    public void generateDefaultID() {
        if (getId() == null) {
            setId(IDUtil.generateASpliceId());
        }
    }

    // 把BaseMgBean转换为id的列表
    public List<String> beanListToIdList(List<BaseMgBean<T>> srcBeans) {
        List<String> rsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(srcBeans)) {
            for (BaseMgBean<T> baseBean : srcBeans) {
                if (StringUtils.isNoneEmpty(baseBean.getId())) {
                    rsList.add(baseBean.getId());
                }
            }
        }
        return rsList;
    }

    // 根据配置的mongo表名称返回mongo表名
    public String findTableNameByAnnotation() {
        Document doc = this.getClass().getAnnotation(Document.class);
        return doc != null ? doc.collection() : null;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(toMongoHashMap());
    }
}

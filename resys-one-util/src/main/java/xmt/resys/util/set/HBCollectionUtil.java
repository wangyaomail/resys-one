package xmt.resys.util.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 这里放一些集合的变换方法
 */
public class HBCollectionUtil {
    public static final List<?> emptyList = new ArrayList<>();

    public static String listToString(Collection<String> list) {
        return listToString(list, ",");
    }

    public static String listToString(String[] list) {
        return listToString(list, ",");
    }

    public static String listToString(String[] list,
                                      String spliter) {
        if (list == null || list.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (String listStr : list) {
            sb.append(listStr).append(spliter);
        }
        return sb.toString();
    }

    public static String listToString(Collection<String> list,
                                      String spliter) {
        if (list == null || list.size() == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (String listStr : list) {
            sb.append(listStr).append(spliter);
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : sb.toString();
    }

    public static List<String> stringToList(String listStr) {
        return stringToList(listStr, ",");
    }

    public static List<String> stringToList(String listStr,
                                            String spliter) {
        if (StringUtils.isEmpty(listStr)) {
            return null;
        }
        String[] listArr = listStr.trim().split(spliter);
        List<String> arrayList = new ArrayList<>(listArr.length);
        for (String str : listArr) {
            arrayList.add(str.trim());
        }
        return arrayList;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> arrayToList(T... arr) {
        List<T> list = new ArrayList<>();
        if (arr != null) {
            for (T t : arr) {
                list.add(t);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> arrayToSet(T... arr) {
        Set<T> set = new HashSet<>();
        if (arr != null) {
            for (T t : arr) {
                set.add(t);
            }
        }
        return set;
    }

    public static <T> ArrayList<T> setToList(Set<T> set) {
        return new ArrayList<T>(set);
    }

    /**
     * 考虑到这种输入下key和value必须是形式一致的对象，这里实现的输入必须类型一致
     * @param arr 参数是"key1","value1","key2","value2"这种形式
     */
    public static Map<String, Object> arrayToMap(Object... kvs) {
        Map<String, Object> rsMap = new HashMap<>();
        if (kvs != null && kvs.length > 0 && kvs.length % 2 == 0) {
            for (int i = 0; i < kvs.length; i++) {
                Object key = kvs[i];
                Object value = kvs[++i];
                if (key != null && value != null) {
                    String keyStr = key.toString().trim();
                    if (HBStringUtil.isNotBlank(keyStr)) {
                        rsMap.put(keyStr, value);
                    }
                }
            }
        }
        return rsMap;
    }

    /**
     * 参数是"key1:value1" "key2:value2"这种形式，返回的map的kv必须是String的父类
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> getMapSemicolon(String... kvs) {
        Map<K, V> rsMap = new HashMap<>();
        if (kvs != null && kvs.length > 0) {
            for (String kv : kvs) {
                String[] kvPair = kv.split(":");
                if (kvPair.length == 2) {
                    rsMap.put((K) kvPair[0].trim(), (V) kvPair[1].trim());
                }
            }
        }
        return rsMap;
    }

    /**
     * 参数是"key1","value1","key2","value2"这种形式，返回的map的kv必须是String的父类
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> getMapSplit(String... kvs) {
        Map<K, V> rsMap = new HashMap<>();
        if (kvs != null && kvs.length > 0 && kvs.length % 2 == 0) {
            for (int i = 0; i < kvs.length; i++) {
                String key = kvs[i];
                String value = kvs[++i];
                if (key != null) {
                    rsMap.put((K) key.trim(), (V) value.trim());
                }
            }
        }
        return rsMap;
    }

    /**
     * 对map按照key进行截断，获取前topk项，asc为ture时，使用升序
     */
    public static <K extends Comparable<K>, V> Map<K, V> getMapTopKByKey(Map<K, V> map,
                                                                         Integer topk,
                                                                         boolean asc) {
        if (MapUtils.isNotEmpty(map) && topk != null && topk > 0) {
            List<Entry<K, V>> list = new ArrayList<Entry<K, V>>(map.entrySet());
            Collections.sort(list, new Comparator<Entry<K, V>>() {
                public int compare(Entry<K, V> o1,
                                   Entry<K, V> o2) {
                    if (asc) {
                        return o1.getKey().compareTo(o2.getKey()); // 升序
                    } else {
                        return o2.getKey().compareTo(o1.getKey()); // 降序
                    }
                }
            });
            return list.stream()
                       .limit(topk)
                       .collect(Collectors.toMap(Entry<K, V>::getKey, Entry<K, V>::getValue));
        }
        return new HashMap<>(1);
    }

    /**
     * 对map按照value进行截断，获取前topk项
     * @param asc ture是升序，false是降序
     */
    public static <K, V extends Comparable<V>> Map<K, V> getMapTopKByValue(Map<K, V> map,
                                                                           Integer topk,
                                                                           boolean asc) {
        if (MapUtils.isNotEmpty(map) && topk != null && topk > 0) {
            List<Entry<K, V>> list = new ArrayList<Entry<K, V>>(map.entrySet());
            Collections.sort(list, new Comparator<Entry<K, V>>() {
                public int compare(Entry<K, V> o1,
                                   Entry<K, V> o2) {
                    if (asc) {
                        return o1.getValue().compareTo(o2.getValue()); // 升序
                    } else {
                        return o2.getValue().compareTo(o1.getValue()); // 降序
                    }
                }
            });
            return list.stream()
                       .limit(topk)
                       .collect(Collectors.toMap(Entry<K, V>::getKey, Entry<K, V>::getValue));
        }
        return new HashMap<>(1);
    }

    /**
     * 把一个不定长数组返回成一个定长数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrToArr(T... arr) {
        return arr;
    }

    /**
     * list集合去重
     */
    public static <T> List<T> listDistinct(List<T> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(j).equals(list.get(i))) {
                    list.remove(j);
                }
            }
        }
        return list;
    }

    /**
     * 数组求和
     */
    public static int sumIntArr(int[] arr) {
        int sum = 0;
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                sum += arr[i];
            }
        }
        return sum;
    }

    public static int sumIntArr(Integer[] arr) {
        int sum = 0;
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                sum += arr[i];
            }
        }
        return sum;
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     * @param inputString 原始字符串
     * @param length      指定长度
     * @return
     */
    public static List<String> getStrList(String inputString,
                                          int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     * @param inputString 原始字符串
     * @param length      指定长度
     * @param size        指定列表大小
     * @return
     */
    public static List<String> getStrList(String inputString,
                                          int length,
                                          int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length, (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空
     * @param str 原始字符串
     * @param f   开始位置
     * @param t   结束位置
     * @return
     */
    public static String substring(String str,
                                   int f,
                                   int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }

    public static boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(final Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isNotEmpty(final Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * 给一个collection内的所有字符串批量加上前缀后缀
     */
    public static Collection<String> addPreAndPostFix(Collection<String> col,
                                                      String prefix,
                                                      String postfix) {
        if (isNotEmpty(col)) {
            return col.stream().map(s -> prefix + s + postfix).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 将map转化为key:value\n的一行一行的字符串
     */
    public static String prettyMapOutput(Map<String, ?> srcMap) {
        StringBuilder sb = new StringBuilder();
        if (isNotEmpty(srcMap)) {
            for (Entry<String, ?> entry : srcMap.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 转化map的时候是否进行排序
     */
    public static String prettyMapOutput(Map<String, ?> srcMap,
                                         Comparator<Entry<String, ?>> comparator) {
        StringBuilder sb = new StringBuilder();
        if (isNotEmpty(srcMap)) {
            if (comparator != null) {
                List<Entry<String, ?>> list = new ArrayList<Entry<String, ?>>(srcMap.entrySet());
                Collections.sort(list, comparator);
                for (Entry<String, ?> entry : list) {
                    sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
                }
            } else {
                return prettyMapOutput(srcMap);
            }
        }
        return sb.toString();
    }

    /**
     * 返回一个数组中的数字的最大值
     */
    public static int argmax(double[] vec) {
        int result = -1;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < vec.length; i++) {
            if (vec[i] > max) {
                max = vec[i];
                result = i;
            }
        }
        return result;
    }

    /**
     * 返回一个数组中的数字的最小值
     */
    public static int argmin(double[] vec) {
        int result = -1;
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < vec.length; i++) {
            if (vec[i] < min) {
                min = vec[i];
                result = i;
            }
        }
        return result;
    }

}

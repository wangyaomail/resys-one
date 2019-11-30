package xmt.resys.util.set;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 长度受限的TreeMap，如果填入的数据超过了最大长度，那么删掉最小的，并且默认状态下不对长度做限制
 */
public class FixedTreeMap<K, V> extends TreeMap<K, V> {
    private static final long serialVersionUID = 7145384210577551700L;
    public int fixedSize = -1;

    public FixedTreeMap() {}

    public FixedTreeMap(int fixedSize) {
        this.fixedSize = fixedSize;
    }

    public FixedTreeMap(Comparator<? super K> comparator,
                        int fixedSize) {
        super(comparator);
        this.fixedSize = fixedSize;
    }

    @Override
    public V put(K key,
                 V value) {
        V res = super.put(key, value);
        while (size() > fixedSize) {
            remove(lastKey());
        }
        return res;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        super.putAll(map);
        while (size() > fixedSize) {
            remove(lastKey());
        }
    }
}

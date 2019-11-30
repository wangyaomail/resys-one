package xmt.resys.common.bean.http;

import java.io.Serializable;
import java.util.Map.Entry;

import lombok.Data;

@Data
public class KeyValuePair<K, V> implements Serializable {
    private static final long serialVersionUID = 2896486324484093129L;
    private K key;
    private V value;

    public KeyValuePair<K, V> fromEntry(Entry<K, V> entry) {
        this.setKey(entry.getKey());
        this.setValue(entry.getValue());
        return this;
    }
}

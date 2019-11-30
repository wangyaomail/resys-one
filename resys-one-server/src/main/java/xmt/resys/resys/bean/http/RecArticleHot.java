package xmt.resys.resys.bean.http;

import java.util.List;
import java.util.stream.Collectors;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.couchbase.BaseCouchbaseBean;

/**
 * 热度推荐结果
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RecArticleHot extends BaseCouchbaseBean<RecArticleHot> {
    protected String id;
    protected Long updateTime; // 更新时间
    protected List<String> hot; // 热度列表

    @Override
    public JsonDocument toJsonDocument(String prefix) {
        JsonObject doc = JsonObject.create()
                                   .put("id", prefix + id)
                                   .put("updateTime", updateTime)
                                   .put("hot", hot);
        return JsonDocument.create(prefix + id, doc);
    }

    @Override
    public RecArticleHot fromDoc(JsonDocument doc,
                                 String prefix) {
        if (doc != null) {
            this.id = doc.content().getString("id").substring(prefix.length());
            this.updateTime = doc.content().getLong("updateTime");
            JsonArray hotArr = doc.content().getArray("hot");
            if (hotArr != null && hotArr.size() > 0) {
                this.hot = hotArr.toList()
                                 .stream()
                                 .map(a -> a.toString())
                                 .collect(Collectors.toList());
            }
        }
        return this;
    }
}

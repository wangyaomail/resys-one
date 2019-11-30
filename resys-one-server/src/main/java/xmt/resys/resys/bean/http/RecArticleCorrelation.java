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
 * 返回给用户相关推荐的内容
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RecArticleCorrelation extends BaseCouchbaseBean<RecArticleCorrelation> {
    protected String id;
    protected Long updateTime; // 发布时间
    protected List<String> cors; // 相关列表

    @Override
    public String getId() {
        return id;
    }

    @Override
    public JsonDocument toJsonDocument(String prefix) {
        JsonObject doc = JsonObject.create()
                                   .put("id", prefix + id)
                                   .put("updateTime", updateTime)
                                   .put("cors", cors);
        return JsonDocument.create(prefix + id, doc);
    }

    @Override
    public RecArticleCorrelation fromDoc(JsonDocument doc,
                                         String prefix) {
        if (doc != null) {  
            this.id = doc.content().getString("id").substring(prefix.length());
            this.updateTime = doc.content().getLong("updateTime");
            JsonArray corArr = doc.content().getArray("cors");
            if (corArr != null && corArr.size() > 0) {
                this.cors = corArr.toList()
                                  .stream()
                                  .map(a -> a.toString())
                                  .collect(Collectors.toList());
            }
        }
        return this;
    }
}

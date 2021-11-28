package xmt.resys.resys.bean.mongo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import xmt.resys.common.bean.mongo.BaseMgBean;

import java.io.Serializable;
import java.util.List;

/**
 * 返回给用户相关推荐的内容
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "rec_article_cor")
public class RecArticleCorrelation extends BaseMgBean<RecArticleCorrelation>
        implements Serializable {
    @Id
    protected String id;
    protected Long updateTime; // 发布时间
    protected List<String> cors; // 相关列表

    //    public JsonObject toJsonObject(String prefix) {
    //        JsonObject doc = JsonObject.create()
    //                                   .put("id", prefix + id)
    //                                   .put("updateTime", updateTime)
    //                                   .put("cors", cors);
    //        return doc;
    //    }
    //
    //    public RecArticleCorrelation fromDoc(
    //            JsonObject doc, String prefix) {
    //        if (doc != null) {
    //            this.id = doc.getString("id").substring(prefix.length());
    //            this.updateTime = doc.getLong("updateTime");
    //            JsonArray corArr = doc.getArray("cors");
    //            if (corArr != null && corArr.size() > 0) {
    //                this.cors = corArr.toList()
    //                                  .stream()
    //                                  .map(a -> a.toString())
    //                                  .collect(Collectors.toList());
    //            }
    //        }
    //        return this;
    //    }
}

package xmt.resys.resys.bean.mongo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import xmt.resys.common.bean.mongo.BaseMgBean;

import java.io.Serializable;
import java.util.List;

/**
 * 热度推荐结果
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "rec_article_hot")
public class RecArticleHot extends BaseMgBean<RecArticleHot> implements Serializable {
    @Id
    protected String id;
    protected Long updateTime; // 更新时间
    protected List<String> hot; // 热度列表

//    public JsonObject toJsonObject(String prefix) {
    //        JsonObject doc = JsonObject.create()
    //                                   .put("id", prefix + id)
    //                                   .put("updateTime", updateTime)
    //                                   .put("hot", hot);
    //        return doc;
    //    }
    //
    //    public RecArticleHot fromDoc(
    //            JsonObject doc, String prefix) {
    //        if (doc != null) {
    //            this.id = doc.getString("id").substring(prefix.length());
    //            this.updateTime = doc.getLong("updateTime");
    //            JsonArray hotArr = doc.getArray("hot");
    //            if (hotArr != null && hotArr.size() > 0) {
    //                this.hot = hotArr.toList()
    //                                 .stream()
    //                                 .map(a -> a.toString())
    //                                 .collect(Collectors.toList());
    //            }
    //        }
    //        return this;
    //    }
}

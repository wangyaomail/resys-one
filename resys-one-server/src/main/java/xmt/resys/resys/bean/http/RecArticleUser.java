package xmt.resys.resys.bean.http;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.couchbase.BaseCouchbaseBean;

/**
 * 用户推荐的对象
 * @INFO 暂时将用户的浏览记录和推荐内容都放到一起，今后系统复杂化以后这块会进行优化
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RecArticleUser extends BaseCouchbaseBean<RecArticleUser> {
    protected String id;
    protected String userid;
    protected String recid; // 推荐行为的订单id，用户发起推荐请求，等于下了一个订单
    protected Long lastReqTime; // 上次访问的时间
    protected List<String> reclist; // 用户的推荐列表
    protected List<String> history; // 用户已经看过的新闻

    public RecArticleUser() {
        reclist = new ArrayList<String>();
        history = new ArrayList<String>();
    }

    public JsonDocument toJsonDocument(String prefix) {
        JsonObject obj = JsonObject.create()
                                   .put("id", prefix + id)
                                   .put("userid", userid)
                                   .put("recid", recid)
                                   .put("lastReqTime", lastReqTime)
                                   .put("reclist", reclist)
                                   .put("history", history);
        return JsonDocument.create(prefix + id, obj);
    }

    public RecArticleUser fromDoc(JsonDocument doc,
                                  String prefix) {
        if (doc != null) {
            this.id = doc.content().getString("id").substring(prefix.length());
            this.userid = doc.content().getString("userid");
            this.recid = doc.content().getString("recid");
            JsonArray atcList = doc.content().getArray("reclist");
            if (atcList != null && atcList.size() > 0) {
                this.reclist = atcList.toList()
                                      .stream()
                                      .map(a -> a.toString())
                                      .collect(Collectors.toList());
            }
            JsonArray history = doc.content().getArray("history");
            if (history != null && history.size() > 0) {
                this.history = history.toList()
                                      .stream()
                                      .map(a -> a.toString())
                                      .collect(Collectors.toList());
            }
        }
        return this;
    }

}

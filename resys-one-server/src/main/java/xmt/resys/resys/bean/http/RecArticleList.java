package xmt.resys.resys.bean.http;

import java.util.List;

import lombok.Data;
import xmt.resys.content.bean.http.HBArticleForList;

/**
 * 返回给用户的推荐结果
 */
@Data
public class RecArticleList {
    private String recid; // 本次推荐任务的id号
    private List<HBArticleForList> list;
}

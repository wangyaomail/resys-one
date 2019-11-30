package xmt.resys.content.bean.http;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.mongo.BaseMgBean;
import xmt.resys.user.bean.http.HBUserBasic;

/**
 * 列表展示用，除了文章正文其它什么都有
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_articles")
public class HBArticleForList extends BaseMgBean<HBArticleForList> implements Serializable {
    private static final long serialVersionUID = 2825125656077687007L;
    @Id
    private String id; // 文章的id号
    private String title; // 文章标题
    private Integer state; // 文章状态
    private Date createTime; // 文章上传日期
    private Date updateTime; // 文章最后一次更新日期
    private Date publishTime;// 发布日期

    private String source;// 文章来源
    private String articleAuthor;// 文章作者
    @Indexed
    private List<String> categorys; // 文章分类
    @Indexed
    private List<String> tag; // 标签
    @DBRef
    private HBUserBasic publisher;// 发布文章作者
}

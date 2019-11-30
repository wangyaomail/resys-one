package xmt.resys.content.bean.mongo;

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
import xmt.resys.content.bean.enums.ArticleStateEnum;
import xmt.resys.user.bean.http.HBUserBasic;
import xmt.resys.util.id.IDUtil;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_articles")
public class HBArticle extends BaseMgBean<HBArticle> implements Serializable {
    private static final long serialVersionUID = -9087214526010481847L;
    @Id
    private String id; // 文章的id号
    private String title; // 文章标题
    private String content; // 文章内容
    private Integer state; // 文章状态
    private Date createTime; // 文章上传日期
    private Date updateTime; // 文章最后一次更新日期
    private Date publishTime; // 发布日期

    private String source; // 文章来源
    private String sourceLink; // 文章来源链接
    private String articleAuthor; // 文章作者
    @Indexed
    private List<String> categorys; // 文章分类
    @Indexed
    private List<String> tags; // 标签
    @DBRef
    private HBUserBasic publisher;// 发布文章作者

    // 下面这些是需要计算的
    private Integer clickNum; // 文章点击数
    private Double qualityScore; // 文章质量评分

    @Override
    public void prepareHBBean() {
        id = id == null ? IDUtil.generateTimedIDStr() : id;
        title = title != null ? title : "未命名";
        // 如果文章新产生的时候就发布，那么发布时间和创建时间相等
        createTime = createTime != null ? createTime : new Date();
        publishTime = publishTime != null ? publishTime : new Date();
        updateTime = updateTime != null ? updateTime : new Date();
        state = state != null ? state : ArticleStateEnum.DRAFT.getIndex();
        clickNum = clickNum != null ? clickNum : 0;
        qualityScore = qualityScore != null ? qualityScore : 0.0;
    }
}

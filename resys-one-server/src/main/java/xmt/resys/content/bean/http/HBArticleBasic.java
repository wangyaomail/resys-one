package xmt.resys.content.bean.http;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "hb_articles")
public class HBArticleBasic implements Serializable {
    private static final long serialVersionUID = 2508505606669276145L;
    @Id
    private String id; // 文章的id号
    private Integer state; // 文章状态
    private Date createTime; // 文章上传日期
    private Date updateTime; // 文章最后一次更新日期
    private Date publishTime;// 发布日期

}

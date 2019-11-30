package xmt.resys.pipeline.bean.mongo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.mongo.BaseMgBean;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_pipeline")
public class HBPipeline extends BaseMgBean<HBPipeline> implements Serializable {
    private static final long serialVersionUID = 2052224114515854461L;
    @Id
    private String id; // 工作流的id号
    private String command; // 执行的语句
    private String period; // 执行周期
    private Boolean valid; // 是否有效
}

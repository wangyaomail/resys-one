package xmt.resys.pipeline.bean.mongo;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.mongo.BaseMgBean;

/**
 * 工作流的执行结果
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_pipeline_result")
public class HBPipelineResult extends BaseMgBean<HBPipeline> implements Serializable {
    private static final long serialVersionUID = 6465918501562128272L;
    @Id
    private String id;
    private Integer state; // 当前周期内的任务执行状态
    private Date finishedTime; // 执行结束的时间

}

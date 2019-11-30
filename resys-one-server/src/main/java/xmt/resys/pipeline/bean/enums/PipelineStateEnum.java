package xmt.resys.pipeline.bean.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工作流任务的执行状态
 */
@Getter
@AllArgsConstructor
public enum PipelineStateEnum {
    WAITING(0, "waiting", "等待"),
    RUNNING(1, "running", "正在运行"),
    ERROR(2, "error", "错误"),
    FINISHED(3, "finished", "已完成");
    private Integer index;
    private String name;
    private String cname;

    public static PipelineStateEnum valueOf(int index) {
        for (PipelineStateEnum serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}

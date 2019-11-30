package xmt.resys.common.bean.enums;

import lombok.Getter;

/**
 * 系統返回时状态码
 */
@Getter
public enum ReturnCode {
    SUCCESS(0, "成功"),
    INTERNAL_ERROR(1, "系统错误");
    private Integer code;
    private String name;

    private ReturnCode(Integer code,
                       String name) {
        this.code = code;
        this.name = name;
    }
}

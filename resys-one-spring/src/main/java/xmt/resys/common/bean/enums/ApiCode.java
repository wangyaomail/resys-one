package xmt.resys.common.bean.enums;

import java.util.Collection;
import java.util.stream.Collectors;

import lombok.Getter;
import xmt.resys.util.set.HBCollectionUtil;

/**
 * 接口返回的状态码
 */
@Getter
public enum ApiCode {
    UNKNOWN("A000", "未知", ""),
    SUCCESS("A001", "成功", ""),
    NO_DATA("A002", "无数据", ""),
    PARAM_FORMAT_ERROR("A003", "参数格式错误", ""),
    PARAM_CONTENT_ERROR("A006", "参数内容错误", ""),
    NO_AUTH("A004", "无权限", ""),
    INTERNAL_ERROR("A005", "系统内部错误", ""),
    AUTH_EXPIRE("A006", "授权过期", ""),
    DUPLICATE_KEY_ERROR("A007", "key值重复", ""),
    API_FREQUENCY_TOO_HIGH("A008", "接口调用太频繁", ""),
    API_REQUEST_ILLEGAL("A009", "非法请求", ""),
    API_NOT_FOUND("A404", "资源不存在", "");
    private String code; // 状态码
    private String name; // 状态名
    private String parent; // 父状态码

    private ApiCode(String code,
                    String name,
                    String parent) {
        this.code = code;
        this.name = name;
        this.parent = parent;
    }

    /**
     * 获取所有的枚举值
     */
    public static Collection<String> all() {
        return HBCollectionUtil.arrayToList(ApiCode.values()).stream().map(e -> {
            return e.getName();
        }).collect(Collectors.toList());
    }
}

package xmt.resys.common.bean.enums;

import java.util.Collection;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xmt.resys.util.set.HBCollectionUtil;

/**
 * 接口的调用场景
 */
@Getter
@AllArgsConstructor
public enum ApiType {
    ALL(0, "all"), // 所有场景
    SAVE(1, "save"), // 保存到数据库
    SEND(2, "send"); // 发送到前端
    private Integer index;
    private String name;

    public static ApiType valueOf(int index) {
        for (ApiType serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }

    /**
     * 获取所有的枚举值
     */
    public static Collection<String> all() {
        return HBCollectionUtil.arrayToList(ApiType.values()).stream().map(e -> {
            return e.getName();
        }).collect(Collectors.toList());
    }
}

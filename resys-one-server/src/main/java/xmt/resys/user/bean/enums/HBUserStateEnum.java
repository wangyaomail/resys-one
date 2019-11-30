package xmt.resys.user.bean.enums;

/**
 * 用户状态信息，是否是发卡用户等
 */
public enum HBUserStateEnum {
    USER_ROLE(1, "正常用户"),
    USER_NOT_ACTIVATE(2, "未激活"),
    USER_ALREADY_ACTIVATE(3, "已经激活");
    private Integer index;
    private String name;

    private HBUserStateEnum(Integer index,
                            String name) {
        this.index = index;
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public static HBUserStateEnum valueOf(int index) {
        for (HBUserStateEnum serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}

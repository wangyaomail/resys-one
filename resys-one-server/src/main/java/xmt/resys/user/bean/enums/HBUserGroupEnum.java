package xmt.resys.user.bean.enums;

/**
 * 一些常用的特殊用户组
 */
public enum HBUserGroupEnum {
    GROUP_EXPERT(1, "expert", "花瓣专家组"),
    GROUP_NORMAL(2, "normal", "普通用户组"),
    GROUP_SELL(3, "sell", "业务接收分配组"),
    GROUP_QASSIGN(4, "qassign", "问答接收分配组"),
    GROUP_QWORK(5, "qwork", "问答管理组");
    private Integer index;
    private String name;
    private String cname;// 中文名

    private HBUserGroupEnum(Integer index,
                            String name,
                            String cname) {
        this.index = index;
        this.name = name;
        this.cname = cname;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getCname() {
        return cname;
    }

    public static HBUserGroupEnum valueOf(int index) {
        for (HBUserGroupEnum serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}

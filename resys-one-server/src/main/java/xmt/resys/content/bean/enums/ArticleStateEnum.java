package xmt.resys.content.bean.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleStateEnum {
    ALL(-1, "all", "不限类型（仅用于查找）"),
    DRAFT(0, "draft", "草稿箱"),
    AUDIT(1, "audit", "待审"),
    POSTED(2, "posted", "待审核"),
    DELETE(3, "delete", "已删除");
    private Integer index;
    private String name;
    private String cname;

    public static ArticleStateEnum valueOf(int index) {
        for (ArticleStateEnum serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}

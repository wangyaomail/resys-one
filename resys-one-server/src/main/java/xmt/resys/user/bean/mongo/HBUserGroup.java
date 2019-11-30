package xmt.resys.user.bean.mongo;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.mongo.BaseMgBean;
import xmt.resys.user.bean.http.HBUserBasic;

/**
 * 用户组 包括用户组的权限、组长、副组长等，用户组的具体组员则由用户表的parent实现方式实现
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_user_groups")
public class HBUserGroup extends BaseMgBean<HBUserGroup> implements Serializable {
    private static final long serialVersionUID = -1812018499878113740L;
    @Id
    private String id; // 组id
    private String name; // 展示名称
    @DBRef
    private HBUserBasic leader; // 组长
    @Indexed
    private List<String> roles; // 该用户组具有的角色
    private String feature; // 该用户组的作用
}

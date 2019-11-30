package xmt.resys.user.bean.http;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.mongo.BaseMgBean;

/**
 * 对HBUser的屏蔽，我们对HBUser的操作都从这里走
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_users")
public class HBUserEdit extends BaseMgBean<HBUserEdit> implements Serializable {
    private static final long serialVersionUID = 4966205186637127270L;
    @Id
    private String id; // 用户id
    private String userName; // 用户名
    private String password; // 用户密码
    private String profile; // 个人简介，长
    private Integer age; // 用户年龄
    private Date birthday; // 用户生日
    private String gender; // 性别
    private Date regDate; // 用户注册日期
    private Boolean valid; // 用户是否有效
    private List<String> roles; // 角色
    private List<String> group; // 所属用户组
}

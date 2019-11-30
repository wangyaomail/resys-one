package xmt.resys.user.bean.http;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.mongo.BaseMgBean;

/**
 * 用户登陆需要的类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_users")
public class HBUserLogin extends BaseMgBean<HBUserLogin> implements Serializable {
    private static final long serialVersionUID = 4034391602063997660L;
    @Id
    private String id; // 用户id
    private String userName; // 用户名
    private String jwtToken; // 用户当前的jwt token，非存数据库
    private Integer age; // 用户年龄
    private String gender; // 性别
    private Date regDate; // 用户注册日期
    private Boolean valid; // 用户是否有效
    private List<String> roles; // 角色
    private List<String> group; // 所属用户组
    @Transient
    private List<String> modules; // 用户所拥有的module，加载的时候填充
}

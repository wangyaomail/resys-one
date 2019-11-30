package xmt.resys.user.bean.http;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.mongo.BaseMgBean;

/**
 * 用于修改用户的登陆信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_users")
public class HBUserAccount extends BaseMgBean<HBUserAccount> implements Serializable {
    private static final long serialVersionUID = -7268794177985060187L;
    @Id
    private String id; // 用户id
    private String userName; // 用户名
    private String password; // 用户输入密码
    private String jwtToken; // 用户当前的jwt token，非存数据库

    // 用户换密码的时候用到
    private String oldPass; // 旧密码
    private String newPass; // 新密码
    private String rePass; // 重复新密码
}
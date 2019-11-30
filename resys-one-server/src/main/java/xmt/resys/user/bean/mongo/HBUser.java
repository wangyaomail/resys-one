package xmt.resys.user.bean.mongo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.mongo.BaseMgBean;
import xmt.resys.content.bean.mongo.HBTag;
import xmt.resys.util.id.IDUtil;

/**
 * 数据库中用户的基础类，承载用户的基础数据
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_users")
public class HBUser extends BaseMgBean<HBUser> implements Serializable {
    private static final long serialVersionUID = -866403171737037890L;
    // 账号信息
    @Id
    private String id; // 用户id
    @Indexed(unique = true)
    private String userName; // 用户登录名，#可登陆#
    private Date regDate; // 用户注册日
    private Date lastPasswordResetDate; // 用户上一次修改密码的日期，jwt要用
    private String password; // 用户密码
    private Boolean valid; // 用户是否有效
    private Date lastLoginDate; // 用户上次登陆的日期
    private String lastLoginIp; // 用户上次登陆IP地址
    private Integer totalLoginTime; // 总共登陆的次数
    // 个性化信息
    private Integer age; // 用户年龄
    private Date birthday; // 用户生日
    private String gender; // 性别
    private String logo; // 用户头像
    private String profile; // 个人简介，长
    // 后台管理功能
    @Indexed
    private List<String> roles; // 角色
    @Indexed
    private List<String> group; // 所属用户组
    // 个人中心
    @DBRef
    private List<HBTag> tags; // 用户标签

    @Override
    public void prepareHBBean() {
        super.prepareHBBean();
        this.id = StringUtils.isEmpty(this.id) ? IDUtil.generateTimedIDStr() : this.id;
        this.userName = StringUtils.isEmpty(this.userName) ? "hb_" + this.id : this.userName;
        this.regDate = new Date();
        this.valid = this.valid != null ? this.valid : true;
    }
}

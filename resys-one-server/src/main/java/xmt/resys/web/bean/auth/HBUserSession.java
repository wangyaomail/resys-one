package xmt.resys.web.bean.auth;

import java.util.List;

import lombok.Data;
import xmt.resys.common.bean.mongo.BaseIdBean;
import xmt.resys.common.service.TimerService;
import xmt.resys.user.bean.mongo.HBUser;

/**
 * 不存数据库，手动创建的用户的基于Redis共享的session
 * @WARN 如果用户修改密码，Session是一定要刷新的
 */
@Data
public class HBUserSession implements BaseIdBean {
    private HBUser user;

    public HBUserSession() {}

    public HBUserSession(HBUser user) {
        this.user = user;
        this.userid = user.getId();
        this.username = user.getUserName();
        this.password = user.getPassword();
        this.lastVisitTime = TimerService.now_to_timestamp;
        this.role = user.getRoles();
    }

    private String id; // session的id就是传给用户的token的前半段
    private String userid;
    private String username;
    private String password;
    private String phoneNum;
    private String token; // 用户这段时间应该用的token
    private List<String> role; // 用户的角色，可以从token中还原
    private List<String> modules; // 用户角色对应的module，不可以从token中还原
    private Long lastVisitTime; // 上次访问的时间
    private String lastVisitUrl; // 上次访问的url
    private boolean expired; // 权限是否过期

    @Override
    public String getId() {
        return id;
    }

}

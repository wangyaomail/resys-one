package xmt.resys.web.controller.auth;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xmt.resys.common.bean.enums.ApiCode;
import xmt.resys.common.bean.http.ResponseBean;
import xmt.resys.common.dao.l1.LocalCacheDao;
import xmt.resys.user.bean.http.HBUserAccount;
import xmt.resys.user.bean.http.HBUserLogin;
import xmt.resys.user.service.UserService;
import xmt.resys.util.set.HBStringUtil;
import xmt.resys.web.bean.auth.HBUserSession;
import xmt.resys.web.controller.BaseController;
import xmt.resys.web.service.AuthService;

@RestController
@RequestMapping(value = { "/${api.version}/f/auth/token", "/${api.version}/b/auth/token" })
public class AuthTokenController extends BaseController {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;
    @Resource
    private LocalCacheDao stringRedisDao;

    /**
     * 使用用户名密码登陆
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/login", method = { RequestMethod.POST })
    public ResponseBean getTokenByNP(@RequestBody HBUserAccount hbuser) {
        ResponseBean responseBean = getReturn();
        // 暂时先用手机号码登陆 by whs
        String username = hbuser.getUserName();
        String password = hbuser.getPassword();
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            try {
                HBUserSession session = authService.login(username, password);
                if (session != null) {
                    HBUserLogin userLogin = userService.getLoginUserInfo(session.getUserid());
                    userLogin.setJwtToken(session.getToken());
                    responseBean.setData(userLogin);
                    userService.recordUserLogin(hbuser.getId(), request.getRemoteAddr());
                } else {
                    responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                    responseBean.setErrMsg("用户名密码不正确");
                }
            } catch (Exception e) {
                logger.debug("登陆失败", e);
                responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
                responseBean.setErrMsg("用户名密码不正确");
            }
        } else {
            logger.debug("用户传来的信息格式错误" + hbuser);
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("登陆信息格式错误");
        }
        return returnBean(responseBean);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/refresh", method = { RequestMethod.GET })
    public ResponseBean refreshToken(HttpServletRequest request) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (HBStringUtil.isNotBlank(userid)) {
            HBUserSession session = authService.loginById(userid);
            responseBean.setOneData("jwtToken", session.getToken());
        } else {
            responseBean.setCodeEnum(ApiCode.NO_AUTH);
        }
        return returnBean(responseBean);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/refreshUser", method = { RequestMethod.GET })
    public ResponseBean refreshUser(HttpServletRequest request) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        HBUserLogin hbuser = userService.getLoginUserInfo(userid);
        responseBean.setData(hbuser);
        // 注意这里没有去刷新jwtToken，也没有修改redis中的内容，可能引起数据不一致
        return returnBean(responseBean);
    }

    /**
     * 使用用户ID密码登陆
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/checkPasswordValid", method = { RequestMethod.POST })
    public ResponseBean checkPasswordValid(@RequestBody HBUserAccount hbuser) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        String password = hbuser.getPassword();
        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(password)) {
            responseBean.setErrMsg("用户没有权限");
            responseBean.setCode(ApiCode.NO_AUTH.getCode());
        } else {
            responseBean.setData(authService.checkPasswordValid(userid, password) != null);
        }
        return returnBean(responseBean);
    }

}

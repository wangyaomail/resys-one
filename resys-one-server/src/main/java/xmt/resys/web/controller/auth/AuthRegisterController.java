package xmt.resys.web.controller.auth;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xmt.resys.common.bean.enums.ApiCode;
import xmt.resys.common.bean.http.KeyValuePair;
import xmt.resys.common.bean.http.ResponseBean;
import xmt.resys.common.dao.l1.LocalCacheDao;
import xmt.resys.user.bean.mongo.HBUser;
import xmt.resys.user.service.UserService;
import xmt.resys.web.bean.auth.HBUserSession;
import xmt.resys.web.controller.BaseController;
import xmt.resys.web.service.AuthService;

@RestController
@RequestMapping(value = "/${api.version}/f/auth/register")
public class AuthRegisterController extends BaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;
    @Resource
    private LocalCacheDao stringLocalCacheDao;

    // 判断用户是否存在
    @RequestMapping(value = "/checkUserExist", method = { RequestMethod.POST })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean checkIfUserExist(@RequestBody KeyValuePair<String, Object> kv) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isEmpty(kv.getKey()) || StringUtils.isEmpty("" + kv.getValue())) {
            responseBean.setErrMsg("传递数据格式有误");
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            return returnBean(responseBean);
        } else {
            responseBean.setOneData("exist",
                                    userService.checkIfExist(kv.getKey(),
                                                             kv.getValue().toString()));
        }
        return returnBean(responseBean);
    }

    /**
     * 验证过验证码后，需要设置密码
     */
    @RequestMapping(value = "", method = { RequestMethod.POST })
    @ResponseStatus(HttpStatus.OK)
    public ResponseBean register(@RequestBody HBUser hbUser) {
        ResponseBean responseBean = getReturn();
        String regPassword = hbUser.getPassword();
        if (StringUtils.isEmpty(regPassword) || regPassword.length() < 6) {
            // 输入的密码格式错误
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("输入密码格式错误");
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("password", regPassword);
            map.put("group", "normal");
            HBUser user = userService.initNewHBUser(map);
            userService.dao().insert(user);
            HBUserSession session = authService.loginById(user.getId());
            responseBean.setOneData("jwtToken", authService.generateToken(session).getToken());
            logger.info("add new User:" + user.toString());
        }
        return returnBean(responseBean);
    }
}

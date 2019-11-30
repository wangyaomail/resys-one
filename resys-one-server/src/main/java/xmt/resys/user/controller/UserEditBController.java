package xmt.resys.user.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.result.UpdateResult;

import xmt.resys.common.bean.anno.RegisterMethod;
import xmt.resys.common.bean.enums.ApiCode;
import xmt.resys.common.bean.http.ResponseBean;
import xmt.resys.user.bean.http.HBUserAccount;
import xmt.resys.user.bean.http.HBUserBasic;
import xmt.resys.user.bean.http.HBUserEdit;
import xmt.resys.user.service.UserService;
import xmt.resys.web.controller.BaseBeanCRUDController;
import xmt.resys.web.service.AuthService;

/**
 * 通过user-edit来实现对用户账户数据的直接修改操作得以屏蔽，能够保护用户的基础数据不动
 */
@RestController
@RequestMapping(value = "/${api.version}/b/user/edit")
@RegisterMethod(methods = { "get" }) // 这样可以动态将方法注册到接口上，能够支持动态方法
public class UserEditBController extends BaseBeanCRUDController<HBUserEdit> {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;

    @RequestMapping(value = "/{func}", method = { RequestMethod.GET })
    public ResponseBean man(@PathVariable String func) {
        return super.man(func);
    }

    @Override
    protected HBUserEdit prepareUpdate(HBUserEdit user,
                                       ResponseBean responseBean) {
        HBUserAccount userAccount = mongoTemplate.findOne(new Query(Criteria.where("id")
                                                                            .is(user.getId())),
                                                          HBUserAccount.class);
        if (userAccount != null && !userAccount.getPassword().equals(user.getPassword())) {
            user.setPassword(authService.encodePassword(user.getPassword()));
        }
        return super.prepareUpdate(user, responseBean);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBUserEdit object) {
        return super.update(object);
    }

    @RequestMapping(value = "/basic/{id}", method = { RequestMethod.GET })
    public ResponseBean getUserBasic(@PathVariable("id") String id) {
        ResponseBean responseBean = getReturn();
        if (StringUtils.isEmpty(id)) {
            responseBean.setCode(ApiCode.PARAM_CONTENT_ERROR.getCode());
            responseBean.setErrMsg("id号未传入");
        } else {
            responseBean.setData(mongoTemplate.findOne(new Query(Criteria.where("id").is(id)),
                                                       HBUserBasic.class,
                                                       "hb_users"));
        }
        return returnBean(responseBean);
    }

    @RequestMapping(value = "/alladmins", method = { RequestMethod.GET })
    public ResponseBean getAllAdmins() {
        ResponseBean responseBean = getReturn();
        responseBean.setData(userService.getAllAdmin());
        return returnBean(responseBean);
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBUserEdit object) {
        return super.query(object);
    }

    /**
     * 将用户密码设为初始状态，管理员调用
     */
    @RegisterMethod
    public String resetPwd() {
        String userid = request.getParameter("userid");
        if (userid != null) {
            Update update = new Update();
            update.set("password",
                       authService.encodePassword(mainServer.conf().getDefaultUserInitPwd()));
            UpdateResult result = mongoTemplate.updateFirst(new Query(Criteria.where("id")
                                                                              .is(userid)),
                                                            update,
                                                            HBUserAccount.class);
            return result.getModifiedCount() > 0 ? "密码重置成功" : "密码重置失败";
        } else {
            return "重置密码失败";
        }
    }

    @RequestMapping(value = "/changPwd", method = { RequestMethod.POST })
    public ResponseBean changPassword(@RequestBody HBUserAccount user) {
        ResponseBean responseBean = getReturn();
        String userid = getUseridFromRequest();
        if (StringUtils.isNotEmpty(userid) && StringUtils.isNotEmpty(user.getNewPass())
                && StringUtils.isNotEmpty(user.getRePass())) {
            if (!user.getNewPass().equals(user.getRePass())) {
                responseBean.setOneData(ApiCode.NO_DATA.getCode(), "信息错误-两次输入密码不一致");
                return responseBean;
            }
            if (authService.checkPasswordValid(userid, user.getOldPass()) != null) {
                Update update = new Update();
                update.set("password", authService.encodePassword(user.getNewPass()));
                mongoTemplate.updateFirst(new Query(Criteria.where("id").is(userid)),
                                          update,
                                          HBUserAccount.class);
            } else {
                responseBean.setOneData(ApiCode.NO_DATA.getCode(), "密码错误");
            }
        } else {
            responseBean.setOneData(ApiCode.NO_DATA.getCode(), "没有查询到有效的用户信息");
        }
        return responseBean;
    }

}

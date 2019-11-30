package xmt.resys.user.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import xmt.resys.common.bean.enums.ApiCode;
import xmt.resys.common.bean.http.ResponseBean;
import xmt.resys.common.service.BaseCRUDService;
import xmt.resys.user.bean.enums.HBRoleEnum;
import xmt.resys.user.bean.mongo.HBUser;
import xmt.resys.user.dao.RoleDao;
import xmt.resys.user.service.UserService;
import xmt.resys.web.controller.BaseCRUDController;

/**
 * 用户修改，正常用户修改，只能修改自己的相关信息
 */
@RestController
@RequestMapping(value = "/${api.version}/b/user")
public class UserBController extends BaseCRUDController<HBUser> {
    @Autowired
    private UserService userService;
    @Resource
    public RoleDao roleDao;

    @Override
    protected BaseCRUDService<HBUser> getService() {
        return userService;
    }

    @Override
    protected HBUser prepareInsert(HBUser object,
                                   ResponseBean responseBean) {
        List<String> group = new ArrayList<>();
        if (object.getGroup() != null) {
            group = object.getGroup();
        }
        group.add(HBRoleEnum.ROLE_STAFF.getName());// 所有用户归为normal用户组
        if (object.getLastPasswordResetDate() != null) {
            // 这个如果不设置，解析jwttoken会出问题
            object.setLastPasswordResetDate(new Date());
        }
        // 最后进行一下重置
        object = userService.initNewHBUser(object.toMongoHashMap());
        object.setGroup(group);
        return super.prepareInsert(object, responseBean);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBUser object) {
        return super.insert(object);
    }

    /**
     * 用户只能更新自己的信息
     */
    @Override
    protected HBUser prepareUpdate(HBUser hbuser,
                                   ResponseBean responseBean) {
        String userid = getUseridFromRequest();
        if (StringUtils.isEmpty(userid)) {
            responseBean.setCodeEnum(ApiCode.NO_AUTH);
        }
        return super.prepareUpdate(hbuser, responseBean);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBUser object) {
        return super.update(object);
    }

    @Override
    protected HBUser postInsert(HBUser object,
                                ResponseBean responseBean) {
        return super.prepareInsert(object, responseBean);
    }

    @Override
    protected String prepareRemove(String id,
                                   ResponseBean responseBean) {
        // 删除钱包
        Map<String, Object> params = new HashMap<>();
        params.put("user", id);
        return super.prepareRemove(id, responseBean);
    }

    @RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
    public ResponseBean remove(@PathVariable("id") String id) {
        return super.remove(id);
    }

}

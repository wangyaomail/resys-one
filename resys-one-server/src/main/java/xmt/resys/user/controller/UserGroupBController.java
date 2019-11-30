package xmt.resys.user.controller;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import xmt.resys.common.bean.anno.RegisterMethod;
import xmt.resys.common.bean.http.ResponseBean;
import xmt.resys.common.service.BaseCRUDService;
import xmt.resys.user.bean.mongo.HBRole;
import xmt.resys.user.bean.mongo.HBUser;
import xmt.resys.user.bean.mongo.HBUserGroup;
import xmt.resys.user.dao.UserDao;
import xmt.resys.user.service.RoleService;
import xmt.resys.user.service.UserGroupService;
import xmt.resys.util.set.HBCollectionUtil;
import xmt.resys.util.set.HBStringUtil;
import xmt.resys.web.controller.BaseCRUDController;

/**
 * 用户组管理
 */
@RestController
@RequestMapping(value = "/${api.version}/b/usergroup")
@RegisterMethod(methods = { "get", "remove" })
public class UserGroupBController extends BaseCRUDController<HBUserGroup> {
    @Autowired
    private UserGroupService userGroupService;
    @Autowired
    private RoleService roleService;
    @Resource
    private UserDao userDao;

    @Override
    protected BaseCRUDService<HBUserGroup> getService() {
        return userGroupService;
    }

    @RequestMapping(value = "/{func}", method = { RequestMethod.GET })
    public ResponseBean man(@PathVariable String func) {
        return super.man(func);
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBUserGroup object) {
        return super.query(object);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBUserGroup object) {
        checkRoleAndLeader(object);
        return super.insert(object);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBUserGroup object) {
        checkRoleAndLeader(object);
        return super.update(object);
    }

    private void checkRoleAndLeader(HBUserGroup object) {
        if (object.getRoles() != null) {// 将前端传来的不合适的role过滤掉
            Collection<HBRole> allRoles = roleService.dao().findAll(object.getRoles(), "id");
            if (HBCollectionUtil.isNotEmpty(allRoles)) {
                object.setRoles(allRoles.stream()
                                        .filter(r -> object.getRoles().contains(r.getId()))
                                        .map(r -> r.getId())
                                        .collect(Collectors.toList()));
            }
        }
        if (object.getLeader() != null
                && HBStringUtil.isNotBlank(object.getLeader().getUserName())) { // 将前端传来的userName变为userid
            HBUser user = userDao.findUserByName(object.getLeader().getUserName());
            if (user != null) {
                object.getLeader().setId(user.getId());
            } else {
                object.setLeader(null);
            }
        }
    }

    /**
     * 获取后台初始化系统需要用到的信息
     */
    @RegisterMethod(name = "init")
    public Collection<HBUserGroup> getInitDatas() {
        return userGroupService.dao().findAll();
    }
}

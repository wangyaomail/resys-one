package xmt.resys.user.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import xmt.resys.common.dao.l3.BaseMongoDao;
import xmt.resys.common.service.BaseCRUDService;
import xmt.resys.user.bean.enums.HBRoleEnum;
import xmt.resys.user.bean.http.HBUserBasic;
import xmt.resys.user.bean.http.HBUserLogin;
import xmt.resys.user.bean.mongo.HBUser;
import xmt.resys.user.dao.UserDao;
import xmt.resys.web.service.AuthService;

@Service
public class UserService extends BaseCRUDService<HBUser> {
    @Resource
    private UserDao userDao;
    @Autowired
    private AuthService authService;

    public BaseMongoDao<HBUser> dao() {
        return userDao;
    }

    @Autowired
    public RoleService roleService;
    @Autowired
    private UserGroupService userGroupService;

    public HBUser initNewHBUser(Map<String, Object> data) {
        HBUser user = new HBUser();
        // 以下条目是新增用户的必备条件，三者必须满足一个，否则不允许新增用户
        if (data.get("userName") != null || data.get("phoneNum") != null
                || data.get("email") != null) {
            user.prepareHBBean();
            if (data.get("userName") != null) {
                user.setUserName(data.get("userName").toString());
            }
            if (data.get("gender") != null) {
                user.setGender(data.get("gender").toString());
            }
            if (data.get("group") != null) {// 初始普通组
                List<String> list = new ArrayList<>();
                list.add(data.get("group").toString());
                user.setGroup(list);
            }
            user.setRegDate(new Date());
            user.setValid(data.containsKey("valid") ? (boolean) data.get("valid") : true);
            if (data.get("password") != null) {
                user.setPassword(authService.encodePassword(data.get("password").toString()));
                user.setLastPasswordResetDate(new Date());
            }
        }
        return user;
    }

    public boolean checkIfExist(String key,
                                String value) {
        HBUser hbUser = null;
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        hbUser = userDao.findOne(params);
        return hbUser == null ? false : true;
    }

    public void updateUserFull(HBUser user) {
        if (StringUtils.isNotEmpty(user.getId())) {
            userDao.updateOne(user.getId(), user.toMongoHashMap());
        }
    }

    // "birthday", "age", "email",
    // "logo","nickName","trueName","profile","comment","gendar"
    public void updateUserLimit(HBUser user) {
        if (StringUtils.isNotEmpty(user.getId())) {
            userDao.updateOne(user.getId(), user.toMongoHashMap());
        }
    }

    public HBUser getUserInfoByUsername(String userName) {
        return userDao.findUserByName(userName);
    }

    public HBUserBasic getUserBasicInfo(String userid) {
        return mongoTemplate.findOne(new Query(Criteria.where("id").is(userid)),
                                     HBUserBasic.class,
                                     userDao.getCollectionName());
    }

    public HBUserLogin getLoginUserInfo(String userid) {
        HBUserLogin user = mongoTemplate.findOne(new Query(Criteria.where("id").is(userid)),
                                                 HBUserLogin.class);
        Set<String> roles = getRoleSetByGroups(user.getGroup());
        if (CollectionUtils.isNotEmpty(user.getRoles())) {
            roles.addAll(user.getRoles());
        }
        List<String> modules = getModuleListByRoles(new ArrayList<>(roles));
        user.setModules(modules);
        return user;
    }

    public String getSessionCode() {
        return RandomStringUtils.random(9, true, true);
    }

    public List<HBUserBasic> getAllAdmin() {
        Query query = Query.query(Criteria.where("roles").in(HBRoleEnum.ROLE_STAFF.getName()));
        return mongoTemplate.find(query, HBUserBasic.class);
    }

    public List<String> getRoleListByGroups(List<String> groups) {
        return new ArrayList<>(getRoleSetByGroups(groups));
    }

    public Set<String> getRoleSetByGroups(List<String> groups) {
        Set<String> roles = new HashSet<>();
        if (CollectionUtils.isNotEmpty(groups)) {
            roles.addAll(userGroupService.getAllUserRolesByGroup(groups));
        }
        return roles;
    }

    public List<String> getModuleListByRoles(List<String> roles) {
        return new ArrayList<>(getModuleSetByRoles(roles));
    }

    public Set<String> getModuleSetByRoles(List<String> roles) {
        Set<String> modules = new HashSet<>();
        if (CollectionUtils.isNotEmpty(roles)) {
            modules.addAll(roleService.getAllModulesByRole(roles));
        }
        return modules;
    }

    /**
     * 记录用户上次登陆的日期
     */
    public void recordUserLogin(String userid,
                                String remoteIp) {
        if (StringUtils.isNotEmpty(userid)) {
            mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(userid)),
                                      new Update().set("lastLoginDate", new Date())
                                                  .set("lastLoginIp", remoteIp)
                                                  .inc("totalLoginTime", 1),
                                      HBUser.class);
        }
    }

    /**
     * 解决其它模块数据调用，这里可以加入截断，对用户的调用数据进行截断并本类的特有功能
     */
    public Object solveData(JSONObject data) {
        return super.solveData(data);
    }

}

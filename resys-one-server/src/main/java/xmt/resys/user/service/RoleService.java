package xmt.resys.user.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import xmt.resys.common.dao.l2.BaseCRUDDao;
import xmt.resys.common.service.BaseCRUDService;
import xmt.resys.user.bean.mongo.HBRole;
import xmt.resys.user.dao.RoleDao;

@Service
public class RoleService extends BaseCRUDService<HBRole> {
    @Resource
    private RoleDao roleDao;

    @Override
    public BaseCRUDDao<HBRole> dao() {
        return roleDao;
    }

    public Set<String> getAllModulesByRole(List<String> roles) {
        Set<String> userModules = new HashSet<>();
        if (CollectionUtils.isNotEmpty(roles)) {
            Collection<HBRole> roleBeans = roleDao.findAll(roles);
            if (CollectionUtils.isNotEmpty(roleBeans)) {
                for (HBRole role : roleBeans) {
                    if (CollectionUtils.isNotEmpty(role.getModules())) {
                        userModules.addAll(role.getModules());
                    }
                }
            }
        }
        return userModules;
    }
}

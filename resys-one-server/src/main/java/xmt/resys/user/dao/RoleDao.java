package xmt.resys.user.dao;

import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l3.BaseLocalMongoCacheDao;
import xmt.resys.user.bean.mongo.HBRole;

@Repository("roleDao")
public class RoleDao extends BaseLocalMongoCacheDao<HBRole> {
}

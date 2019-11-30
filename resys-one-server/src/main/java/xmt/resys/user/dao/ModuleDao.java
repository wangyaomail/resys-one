package xmt.resys.user.dao;

import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l3.BaseLocalMongoCacheDao;
import xmt.resys.user.bean.mongo.HBModule;

@Repository("moduleDao")
public class ModuleDao extends BaseLocalMongoCacheDao<HBModule> {
}

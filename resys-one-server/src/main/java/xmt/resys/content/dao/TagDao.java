package xmt.resys.content.dao;

import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l4.BaseTreeLocalMongoCacheDao;
import xmt.resys.content.bean.mongo.HBTag;

@Repository("tagDao")
public class TagDao extends BaseTreeLocalMongoCacheDao<HBTag> {
}

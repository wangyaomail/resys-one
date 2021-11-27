package xmt.resys.resys.dao;

import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l3.BaseMongoDao;
import xmt.resys.common.service.TimerService;
import xmt.resys.resys.bean.mongo.RecArticleUser;

@Repository("recArticleUserDao")
public class RecArticleUserDao extends BaseMongoDao<RecArticleUser> {

    @Override
    public RecArticleUser insert(RecArticleUser object) {
        object.setLastReqTime(TimerService.now_to_timestamp);
        return super.insert(object);
    }

}

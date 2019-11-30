package xmt.resys.resys.dao;

import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l2.BaseCouchbaseDao;
import xmt.resys.common.service.TimerService;
import xmt.resys.resys.bean.http.RecArticleUser;

@Repository("recArticleUserCouchbaseDao")
public class RecArticleUserCouchbaseDao extends BaseCouchbaseDao<RecArticleUser> {

    @Override
    public String getPrefix() {
        return mainServer.conf().getCouchbaseRecArticleUserPrefix();
    }

    @Override
    public Integer getTTL() {
        return mainServer.conf().getCouchbaseRecArticleUserTTL();
    }

    @Override
    public RecArticleUser insert(RecArticleUser object) {
        object.setLastReqTime(TimerService.now_to_timestamp);
        return super.insert(object);
    }

}

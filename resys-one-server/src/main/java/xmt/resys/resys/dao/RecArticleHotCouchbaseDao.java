package xmt.resys.resys.dao;

import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l2.BaseCouchbaseDao;
import xmt.resys.resys.bean.http.RecArticleHot;

@Repository("recArticleHotCouchbaseDao")
public class RecArticleHotCouchbaseDao extends BaseCouchbaseDao<RecArticleHot> {

    @Override
    public String getPrefix() {
        return mainServer.conf().getCouchbaseRecArticleHotPrefix();
    }

    @Override
    public Integer getTTL() {
        return mainServer.conf().getCouchbaseRecArticleHotTTL();
    }

}

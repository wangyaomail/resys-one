package xmt.resys.resys.dao;

import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l2.BaseCouchbaseDao;
import xmt.resys.resys.bean.http.RecArticleCorrelation;

@Repository("recArticleCorrelationCouchbaseDao")
public class RecArticleCorrelationCouchbaseDao extends BaseCouchbaseDao<RecArticleCorrelation> {

    @Override
    public String getPrefix() {
        return mainServer.conf().getCouchbaseRecArticleCorrelationPrefix();
    }

    @Override
    public Integer getTTL() {
        return mainServer.conf().getCouchbaseRecArticleCorrelationTTL();
    }

}

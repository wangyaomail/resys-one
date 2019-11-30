package xmt.resys.content.dao;

import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l2.BaseActiveMQDao;

@Repository("articleChangeSendDao")
public class ArticleChangeSendDao extends BaseActiveMQDao {

    @Override
    public boolean isQueue() {
        return true;
    }

    @Override
    public boolean isTransacted() {
        return true;
    }

    @Override
    public String getQueueName() {
        return "videobase-add";
    }

    @Override
    public String getTopicDurableName() {
        return "resys-videobase-add-duration";
    }

    @Override
    public String getRuningStrategy() {
        return "producer";
    }

}

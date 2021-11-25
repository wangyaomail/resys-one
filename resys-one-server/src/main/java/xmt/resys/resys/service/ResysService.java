package xmt.resys.resys.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import xmt.resys.common.service.BaseService;
import xmt.resys.common.service.TimerService;
import xmt.resys.content.bean.http.HBArticleBasic;
import xmt.resys.content.bean.http.HBArticleForList;
import xmt.resys.content.service.ArticleService;
import xmt.resys.resys.bean.http.RecArticleList;
import xmt.resys.resys.bean.kafka.UserListClickRecord;
import xmt.resys.resys.bean.mongo.RecArticleCorrelation;
import xmt.resys.resys.bean.mongo.RecArticleHot;
import xmt.resys.resys.bean.mongo.RecArticleUser;
import xmt.resys.resys.dao.RecArticleCorrelationDao;
import xmt.resys.resys.dao.RecArticleHotDao;
import xmt.resys.resys.dao.RecArticleUserDao;
import xmt.resys.resys.dao.RecUserListClickKafkaDao;
import xmt.resys.util.id.IDUtil;
import xmt.resys.util.set.HBCollectionUtil;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理用户送来的推荐请求
 *
 * @WARN 用户推荐信息处理较为复杂，所以默认传入这里的数据都是OK的，数据完整性风险由controller全部负责
 */
@Getter
@Service
public class ResysService extends BaseService {

    @Resource
    private MongoTemplate mongoTemplate;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private ResysService resysService;
    @Resource
    private RecArticleUserDao recArticleUserDao;
    @Resource
    private RecArticleHotDao recArticleHotDao;
    @Resource
    private RecArticleCorrelationDao recArticleCorrelationDao;
    @Resource
    private RecUserListClickKafkaDao recUserListClickKafkaDao;

    /**
     * @IMPORTANT 暂时忽略性能问题，线上必须补上
     */
    public RecArticleList getUserRecList(
            String userid, int feedNumber) {
        RecArticleList reclist = new RecArticleList();
        // 连接Mongo获取用户的推荐结果
        RecArticleUser rau = recArticleUserDao.findOne(userid);
        if (rau != null) {
            // 获取前feedNumber的推荐内容
            List<String> recIds;
            if (feedNumber > rau.getReclist().size()) {
                recIds = rau.getReclist();
            } else {
                recIds = new ArrayList<String>(rau.getReclist().subList(0, feedNumber));
            }
            // 根据结果从数据库内查询文章
            reclist.setList(mongoTemplate.find(Query.query(Criteria.where("id").in(recIds)),
                                               HBArticleForList.class));
            // 将推荐的内容从待推荐列表中删除
            rau.getReclist().removeAll(recIds);
            // 将推荐的内容写入history
            rau.getHistory().addAll(recIds);
            // 如果历史记录长度过长，那么进行删减
            if (rau.getHistory().size() > sysConfService.getFeedHistoryMaxLength()) {
                rau.setHistory(new ArrayList<String>(rau.getHistory()
                                                        .subList(rau.getHistory()
                                                                    .size() - sysConfService.getFeedHistoryMaxLength(),
                                                                 rau.getHistory().size())));
            }
            // 为本次推荐写入新的订单id
            reclist.setRecid(genRecid());
            // 重新写入Mongo
            recArticleUserDao.insert(rau);
        }
        return reclist;
    }

    public String genRecid() {
        return IDUtil.generateOrderIdStr(IDUtil.generateRandomKey(9));
    }

    /**
     * 随机从数据库中取feedNumber条feed
     *
     * @warn 这个方法效率很低，不可用于正常业务使用，仅用于手动测试
     */
    public int randomInsertFeed(
            String userid, int feedNumber) {
        // 获取用户当前的推荐信息
        RecArticleUser rau = recArticleUserDao.findOne(userid);
        if (rau == null || rau.getId() == null) {
            rau = new RecArticleUser();
            rau.setId(userid);
            rau.setUserid(userid);
        }
        // 获取所有用于测试的文档
        List<HBArticleBasic> abs = mongoTemplate.find(Query.query(Criteria.where("categorys")
                                                                          .in("来源-测试")),
                                                      HBArticleBasic.class);
        List<String> ids = abs.stream().map(a -> a.getId()).collect(Collectors.toList());
        if (HBCollectionUtil.isNotEmpty(rau.getHistory())) {
            // 如果用户已经有浏览记录，那么删掉
            ids.removeAll(rau.getHistory());
        }
        if (HBCollectionUtil.isNotEmpty(rau.getReclist())) {
            // 如果这些内容已经是待推荐的，那么删掉
            ids.removeAll(rau.getReclist());
        }
        Collections.shuffle(ids);
        if (feedNumber < ids.size()) {
            ids = ids.subList(0, feedNumber);
        }
        rau.getReclist().addAll(ids);
        // 将推荐结果写入Mongo
        recArticleUserDao.insert(rau);
        return ids.size();
    }

    /**
     * 清空用户的浏览记录
     */
    public boolean clearUserHistory(String userid) {
        // 获取用户当前的推荐信息
        RecArticleUser rau = recArticleUserDao.findOne(userid);
        // 清空历史记录
        rau.setHistory(new ArrayList<String>());
        // 重新写入Mongo
        recArticleUserDao.insert(rau);
        return true;
    }

    /**
     * 把用户的请求记录发送到后台
     */
    public void sendUserListClickRecord(UserListClickRecord record) {
        recUserListClickKafkaDao.asyncSend(record.getId(), record.toJSONString());
    }

    /**
     * 从Mongo请求当天的热度文件
     */
    public List<HBArticleForList> getArticleHot(int limit) {
        List<HBArticleForList> hots = new ArrayList<HBArticleForList>();
        RecArticleHot articleHot = recArticleHotDao.findOne(TimerService.now_to_day);
        if (articleHot == null || articleHot.getHot() == null) {
            // 如果按照日期获得失败，那么按照通用符获取
            articleHot = recArticleHotDao.findOne("news_list");
        }
        if (articleHot != null && articleHot.getHot() != null) {
            // 按照limit做截断
            List<String> ids = new ArrayList<String>();
            if (ids.size() > limit) {
                ids.addAll(articleHot.getHot().subList(0, limit));
            } else {
                ids.addAll(articleHot.getHot());
            }
            // 填充进具体的文章
            hots.addAll(mongoTemplate.find(Query.query(Criteria.where("id").in(ids)),
                                           HBArticleForList.class));
        }
        return hots;
    }

    /**
     * 从Mongo请求相关新闻
     */
    public List<HBArticleForList> getArticleCorrelation(
            String articleId, int limit) {
        List<HBArticleForList> corrs = new ArrayList<HBArticleForList>();
        RecArticleCorrelation articleCorrelation = recArticleCorrelationDao.findOne(articleId);
        if (articleCorrelation != null && articleCorrelation.getCors() != null) {
            // 按照limit做截断
            List<String> ids = new ArrayList<String>();
            if (ids.size() > limit) {
                ids.addAll(articleCorrelation.getCors().subList(0, limit));
            } else {
                ids.addAll(articleCorrelation.getCors());
            }
            // 填充进具体的文章
            corrs.addAll(mongoTemplate.find(Query.query(Criteria.where("id").in(ids)),
                                            HBArticleForList.class));
        }
        return corrs;
    }

}

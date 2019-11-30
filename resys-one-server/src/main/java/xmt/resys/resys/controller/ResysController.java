package xmt.resys.resys.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import xmt.resys.common.bean.anno.RegisterMethod;
import xmt.resys.common.bean.http.ResponseBean;
import xmt.resys.content.bean.http.HBArticleForList;
import xmt.resys.content.service.ArticleService;
import xmt.resys.resys.bean.http.RecArticleList;
import xmt.resys.resys.bean.kafka.UserListClickRecord;
import xmt.resys.resys.service.ResysService;
import xmt.resys.util.set.HBStringUtil;
import xmt.resys.web.controller.BaseController;

/**
 * 生成前端的推荐结果
 */
@RestController
@RequestMapping(value = { "/${api.version}/b/resys" })
public class ResysController extends BaseController {
    @Autowired
    private ResysService resysService;
    @Autowired
    private ArticleService articleService;

    @Override
    @RequestMapping(value = "/{func}", method = { RequestMethod.GET })
    public ResponseBean man(@PathVariable String func) {
        return super.man(func);
    }

    /**
     * 获得自己的推荐结果，这一步是最主要的方法
     */
    @RegisterMethod
    public RecArticleList getRecList() {
        // 解析出前端申请的feednum数量
        Integer feedNumber = mainServer.conf().getFeedNumberDefault();
        try {
            feedNumber = Integer.parseInt(request.getParameter("feedNumber"));
        } catch (Exception e) {
        }
        feedNumber = feedNumber > mainServer.conf().getFeedNumberMax()
                ? mainServer.conf().getFeedNumberMax()
                : feedNumber;
        String userid = getUseridFromRequest();
        RecArticleList reclist = resysService.getUserRecList(userid, feedNumber);
        return reclist;
    }

    /**
     * 向couchbase填入随机的推荐结果
     */
    @RegisterMethod
    public String randomGenerateFeed() {
        try {
            Integer feedNumber = Integer.parseInt(request.getParameter("feedNumber"));
            String userid = getUseridFromRequest();
            int genNum = resysService.randomInsertFeed(userid, feedNumber);
            return "随机生成了" + genNum + "条推荐结果";
        } catch (Exception e) {
            logger.error("解析参数失败", e);
            return "随机生成推荐结果失败";
        }
    }

    /**
     * 清空用户的浏览记录
     */
    @RegisterMethod
    public String clearUserHistory() {
        try {
            String userid = getUseridFromRequest();
            if (resysService.clearUserHistory(userid)) {
                return "用户访问记录已清除";
            }
        } catch (Exception e) {
            logger.error("解析参数失败", e);
        }
        return "用户访问记录清除失败";
    }

    /**
     * 用户获取新闻的新接口
     */
    @RequestMapping(value = "/getArticle", method = { RequestMethod.POST })
    public ResponseBean getArticle(@RequestBody UserListClickRecord object) {
        ResponseBean responseBean = getReturn();
        if (HBStringUtil.isNotBlank(object.getClick())) {
            if (HBStringUtil.isNotBlank(object.getRecid(), object.getClick())) {
                object.setUserid(getUseridFromRequest());
                // 使用订单+本次点击来初始化此次点击行为，这样一定程度上能弱化重复点击的情况
                object.setId(object.getRecid() + "-" + object.getClick());
                object.setClickTime(System.currentTimeMillis());
                resysService.sendUserListClickRecord(object);
            }
            responseBean.setData(articleService.dao().findOne(object.getClick()));
        }
        return returnBean(responseBean);
    }

    /**
     * 用户获取新闻的相关新闻
     */
    @RegisterMethod
    public List<HBArticleForList> getArticleCorrelation() {
        List<HBArticleForList> articleList = new ArrayList<HBArticleForList>();
        String articleId = request.getParameter("articleId");
        Integer limit = null;
        try {
            limit = Integer.parseInt(request.getParameter("limit"));
        } catch (Exception e) {
        }
        limit = limit == null ? Integer.MAX_VALUE : limit;
        if (limit != null && articleId != null) {
            articleList = resysService.getArticleCorrelation(articleId, limit);
        }
        return articleList;
    }

    /**
     * 用户获取热度新闻
     */
    @RegisterMethod
    public RecArticleList getArticleHot() {
        RecArticleList hotNews = new RecArticleList();
        Integer limit = null;
        try {
            limit = Integer.parseInt(request.getParameter("limit"));
        } catch (Exception e) {
        }
        limit = limit == null ? Integer.MAX_VALUE : limit;
        hotNews.setList(resysService.getArticleHot(limit));
        hotNews.setRecid(resysService.genRecid());
        return hotNews;
    }

}

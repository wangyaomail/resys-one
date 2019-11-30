package xmt.resys.content.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import xmt.resys.common.bean.anno.RegisterMethod;
import xmt.resys.common.bean.http.ResponseBean;
import xmt.resys.common.bean.mongo.BaseMgBean;
import xmt.resys.common.service.BaseCRUDService;
import xmt.resys.content.bean.http.HBArticleForList;
import xmt.resys.content.bean.mongo.HBArticle;
import xmt.resys.content.dao.ArticleChangeSendDao;
import xmt.resys.content.service.ArticleService;
import xmt.resys.user.bean.http.HBUserBasic;
import xmt.resys.util.set.HBCollectionUtil;
import xmt.resys.util.set.HBStringUtil;
import xmt.resys.web.controller.BaseCRUDController;

@RestController
@RequestMapping(value = { "/${api.version}/b/article" })
@RegisterMethod(methods = { "get", "remove" })
public class ArticleBController extends BaseCRUDController<HBArticle> {
    @Resource
    private ArticleChangeSendDao articleChangeSendDao;
    @Autowired
    private ArticleService articleService;

    @Override
    protected BaseCRUDService<HBArticle> getService() {
        return articleService;
    }

    @Override
    @RequestMapping(value = "/{func}", method = { RequestMethod.GET })
    public ResponseBean man(@PathVariable String func) {
        return super.man(func);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBArticle object) {
        return super.update(object);
    }

    @Override
    protected HBArticle prepareInsert(HBArticle object,
                                      ResponseBean responseBean) {
        String userid = getUseridFromRequest();
        HBUserBasic user = new HBUserBasic(userid != null ? userid
                : mainServer.conf().getAnonymousUser());
        object.setPublisher(user);
        return super.prepareInsert(object, responseBean);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBArticle object) {
        return super.insert(object);
    }

    /**
     * 假删除
     */
    @RegisterMethod
    public String fakedel() {
        String id = request.getParameter("id");
        if (HBStringUtil.isNotBlank(id)) {
            if (articleService.dao().updateOne(id, HBCollectionUtil.arrayToMap("state", 3))) {
                return "已将文章移至回收站";
            }
        }
        return "删除文章失败";
    }

    /**
     * 导出所有article为一个文件
     */
    @RegisterMethod
    public String exportAllArticle() {
        if (articleService.exportAllArticle()) {
            return "生成完毕";
        } else {
            return "正在生成中，请勿重新生成";
        }
    }

    @Override
    protected List<String> prepareQueryFields(HBArticle object,
                                              ResponseBean responseBean) {
        if (object.getState() == -1) {
            object.setState(null);
        }
        List<String> fields = BaseMgBean.generateFieldListToList(HBArticleForList.class);
        return fields;
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBArticle object) {
        return super.query(object);
    }

    /**
     * 如果产生修改等操作，发消息给activemq，以下三个方法同理
     */
    @Override
    protected HBArticle postInsert(HBArticle object,
                                   ResponseBean responseBean) {
        postUpdate(object, responseBean);
        return super.postInsert(object, responseBean);
    }

    @Override
    public void postUpdate(HBArticle object,
                           ResponseBean responseBean) {
        if (object != null) {
            JSONObject jobj = new JSONObject(object.toMongoHashMap());
            try {
                articleChangeSendDao.sendMessage(jobj.toJSONString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("json转码出错", e);
            }
        }
        super.postUpdate(object, responseBean);
    }

    @Override
    public void postRemove(String id,
                           ResponseBean responseBean) {
        if (HBStringUtil.isNotBlank(id)) {
            JSONObject jobj = new JSONObject();
            jobj.put("id", id);
            jobj.put("delete", true);
            try {
                articleChangeSendDao.sendMessage(jobj.toJSONString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("json转码出错", e);
            }
        }
        super.postRemove(id, responseBean);
    }

}

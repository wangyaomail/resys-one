package xmt.resys.content.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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
import xmt.resys.common.service.BaseCRUDService;
import xmt.resys.content.bean.mongo.HBTag;
import xmt.resys.content.dao.TagChangeSendDao;
import xmt.resys.content.service.TagService;
import xmt.resys.util.set.HBStringUtil;
import xmt.resys.web.controller.BaseCRUDController;

/**
 * 标签类
 */
@RestController
@RequestMapping(value = { "/${api.version}/b/tag" })
@RegisterMethod(methods = { "get", "remove" })
public class TagBController extends BaseCRUDController<HBTag> {
    @Resource
    private TagChangeSendDao tagChangeSendDao;
    @Autowired
    private TagService tagService;

    @Override
    protected BaseCRUDService<HBTag> getService() {
        return tagService;
    }

    @Override
    @RequestMapping(value = "/{func}", method = { RequestMethod.GET })
    public ResponseBean man(@PathVariable String func) {
        return super.man(func);
    }

    @RequestMapping(value = "/upsert", method = { RequestMethod.POST })
    public ResponseBean upsert(@RequestBody HBTag object) {
        if (HBStringUtil.isNotBlank(object.getOldId())) {
            tagService.updateId(object);
        }
        // 再修改其它的
        return super.upsert(object);
    }

    /**
     * 根据parent查询所有子类标签
     */
    @RegisterMethod(name = "parent")
    public Collection<HBTag> getTagsByParent() {
        String parent = request.getParameter("parent");
        if (HBStringUtil.isNotBlank(parent)) {
            return tagService.findAllTagByParent(parent);
        }
        return new ArrayList<HBTag>(1);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBTag object) {
        return super.insert(object);
    }

    /**
     * 添加多个标签，按照-进行区分
     */
    @RequestMapping(value = "", method = { RequestMethod.POST })
    public ResponseBean addTags(@RequestBody HBTag tagParam) {
        ResponseBean responseBean = getReturn();
        if (tagParam.getId().indexOf("-") != -1) {
            String[] tagArray = tagParam.getId().split("-");
            for (String tagName : tagArray) {
                HBTag tag = new HBTag();
                tag.setParent(tagParam.getParent());
                tag.setId(tagName);
                tagService.dao().insert(tag);
            }
        } else {
            tagService.dao().insert(tagParam);
        }
        return returnBean(responseBean);
    }

    /**
     * 删除这个标签以及这个标签的所有子分类
     */
    @RegisterMethod(name = "delrecursive")
    public String delrecursive() {
        String id = request.getParameter("id");
        if (HBStringUtil.isNotBlank(id)) {
            if (tagService.recursiveDelete(id)) {
                return "删除成功";
            }
        }
        return "删除失败";
    }

    /**
     * 获取后台初始化系统需要用到的标签 默认获取2层的标签
     */
    @RegisterMethod(name = "init")
    public Map<String, HBTag> getInitDatas() {
        return tagService.getAllTree();
    }

    /**
     * 如果产生修改等操作，发消息给activemq，以下三个方法同理
     */
    @Override
    protected HBTag postInsert(HBTag object,
                               ResponseBean responseBean) {
        postUpdate(object, responseBean);
        return super.postInsert(object, responseBean);
    }

    @Override
    public void postUpdate(HBTag object,
                           ResponseBean responseBean) {
        if (object != null) {
            JSONObject jobj = new JSONObject(object.toMongoHashMap());
            try {
                tagChangeSendDao.sendMessage(jobj.toJSONString().getBytes("UTF-8"));
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
                tagChangeSendDao.sendMessage(jobj.toJSONString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("json转码出错", e);
            }
        }
        super.postRemove(id, responseBean);
    }

    /**
     * 将所有的标签都向后传递
     */
    @RegisterMethod(name = "sendAllTags")
    public String sendAllTags() {
        Collection<HBTag> tags = tagService.dao().findAll();
        for (HBTag tag : tags) {
            try {
                tagChangeSendDao.sendMessage(new JSONObject(tag.toMongoHashMap()).toJSONString()
                                                                                 .getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("解析标签失败: " + tag.getId());
            }
        }
        return "标签向后传递成功";
    }
}

package xmt.resys.web.controller;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import xmt.resys.common.bean.anno.RegisterMethod;
import xmt.resys.common.bean.http.ResponseBean;
import xmt.resys.common.dao.l1.LocalCacheDao;
import xmt.resys.common.service.TimerService;
import xmt.resys.user.service.ModuleService;
import xmt.resys.user.service.RoleService;
import xmt.resys.user.service.UserGroupService;

/**
 * 返回数据常用字典数据
 */
@RestController
@RequestMapping(value = { "/${api.version}/b/dictionary" })
public class DictionaryController extends BaseController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private UserGroupService userGroupService;
    @Resource
    private LocalCacheDao localCacheDao;

    @Override
    @RequestMapping(value = "/{func}", method = { RequestMethod.GET })
    public ResponseBean man(@PathVariable String func) {
        return super.man(func);
    }

    /**
     * 获取后台初始化系统需要用到的信息
     */
    @RegisterMethod()
    public JSONObject init() {
        String dicKey = "system-dictionary";
        JSONObject dicJson = (JSONObject) localCacheDao.get(dicKey);
        // 检查是否过期，手动实现5min的过期
        if (dicJson != null
                && dicJson.getLongValue("time") - TimerService.now_to_timestamp < 5l * 60 * 1000) {
            dicJson = null;
        }
        if (dicJson == null) {
            dicJson = new JSONObject();
            dicJson.put("time", TimerService.now_to_timestamp);
            dicJson.put("roles", roleService.dao().findAll());
            dicJson.put("modules", moduleService.dao().findAll());
            dicJson.put("groups", userGroupService.dao().findAll());
            localCacheDao.put(dicKey, dicJson);
        }
        return dicJson;
    }

}

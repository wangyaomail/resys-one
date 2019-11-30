package xmt.resys.user.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import xmt.resys.common.bean.anno.RegisterMethod;
import xmt.resys.common.bean.http.ResponseBean;
import xmt.resys.common.service.BaseCRUDService;
import xmt.resys.user.bean.mongo.HBModule;
import xmt.resys.user.service.ModuleService;
import xmt.resys.web.controller.BaseCRUDController;

/**
 * 模块管理
 */
@RestController
@RequestMapping(value = "/${api.version}/b/module")
@RegisterMethod(methods = { "get", "remove" })
public class ModuleBController extends BaseCRUDController<HBModule> {
    @Autowired
    private ModuleService moduleService;

    @Override
    protected BaseCRUDService<HBModule> getService() {
        return moduleService;
    }

    @RequestMapping(value = "/{func}", method = { RequestMethod.GET })
    public ResponseBean man(@PathVariable String func) {
        return super.man(func);
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBModule object) {
        return super.query(object);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBModule object) {
        return super.insert(object);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBModule object) {
        return super.update(object);
    }

    /**
     * 获取后台初始化系统需要用到的信息
     */
    @RegisterMethod(name = "init")
    public Collection<HBModule> getInitDatas() {
        return moduleService.dao().findAll();
    }
}

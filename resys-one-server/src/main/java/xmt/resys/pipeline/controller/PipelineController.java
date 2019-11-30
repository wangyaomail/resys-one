package xmt.resys.pipeline.controller;

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
import xmt.resys.pipeline.bean.mongo.HBPipeline;
import xmt.resys.pipeline.service.PipelineService;
import xmt.resys.web.controller.BaseCRUDController;

@RestController
@RequestMapping(value = "/${api.version}/b/pipeline")
@RegisterMethod(methods = { "get", "remove" })
public class PipelineController extends BaseCRUDController<HBPipeline> {
    @Autowired
    private PipelineService pipelineService;

    @Override
    protected BaseCRUDService<HBPipeline> getService() {
        return pipelineService;
    }

    @RequestMapping(value = "/{func}", method = { RequestMethod.GET })
    public ResponseBean man(@PathVariable String func) {
        return super.man(func);
    }

    @RequestMapping(value = "", method = { RequestMethod.PUT })
    public ResponseBean insert(@RequestBody HBPipeline object) {
        return super.insert(object);
    }

    @RequestMapping(value = "/update", method = { RequestMethod.POST })
    public ResponseBean update(@RequestBody HBPipeline object) {
        return super.update(object);
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseBean query(@RequestBody HBPipeline object) {
        return super.query(object);
    }

    @RegisterMethod(name = "init")
    public Collection<HBPipeline> getInitDatas() {
        return getService().dao().findAll();
    }
}

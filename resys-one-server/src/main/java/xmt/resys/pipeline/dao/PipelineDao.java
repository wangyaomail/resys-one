package xmt.resys.pipeline.dao;

import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l3.BaseMongoDao;
import xmt.resys.pipeline.bean.mongo.HBPipeline;

@Repository("pipelineDao")
public class PipelineDao extends BaseMongoDao<HBPipeline> {
}

package xmt.resys.pipeline.service;

import java.util.Collection;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import xmt.resys.common.dao.l2.BaseCRUDDao;
import xmt.resys.common.service.BaseCRUDService;
import xmt.resys.pipeline.bean.mongo.HBPipeline;
import xmt.resys.pipeline.dao.PipelineDao;
import xmt.resys.pipeline.dao.PipelineResultDao;

@Service
public class PipelineService extends BaseCRUDService<HBPipeline> {

    @Resource
    private PipelineDao pipelineDao;
    @Resource
    private PipelineResultDao pipelineResultDao;

    @Override
    public BaseCRUDDao<HBPipeline> dao() {
        return pipelineDao;
    }

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 定时检查所有任务是否执行成功
     * @INFO 每5min一次，这个时间可以按照需要调整
     */
    @Scheduled(cron = "0 */2 * * * ?")
    public boolean exportAllArticle() {
        if (lock.writeLock().tryLock()) {
            Collection<HBPipeline> ppls = pipelineDao.findAll("valid", true);
            System.out.println(ppls);
            return true;
        } else {
            return false;
        }
    }

}

package xmt.resys.content.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Resource;

import org.bson.Document;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.util.CloseableIterator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import xmt.resys.common.dao.l2.BaseCRUDDao;
import xmt.resys.common.service.BaseCRUDService;
import xmt.resys.common.service.TimerService;
import xmt.resys.content.bean.mongo.HBArticle;
import xmt.resys.content.dao.ArticleCategoryDao;
import xmt.resys.content.dao.ArticleDao;

@Service
public class ArticleService extends BaseCRUDService<HBArticle> {
    @Resource
    private ArticleDao articleDao;
    @Resource
    private ArticleCategoryDao articleCategoryDao;

    private ReentrantReadWriteLock exportLock = new ReentrantReadWriteLock();

    public BaseCRUDDao<HBArticle> dao() {
        return articleDao;
    }

    /**
     * 将所有article导出为一个文件
     */
//    @Scheduled(cron = "0 1 * * * *")
    public boolean exportAllArticle() {
        if (exportLock.writeLock().tryLock()) {
            File videobaseFile = new File(sysConfService.getVideobaseAllPath() + "/videobase_all_"
                    + TimerService.now_to_day + ".data");
            if (!videobaseFile.exists()) {
                try {
                    File dir = new File(sysConfService.getVideobaseAllPath());
                    dir.mkdirs();
                    videobaseFile.createNewFile();
                } catch (IOException e) {
                }
            }
            try (FileWriter fw = new FileWriter(videobaseFile);
                    CloseableIterator<HBArticle> itr = mongoTemplate.stream(new BasicQuery(new Document()),
                                                                            HBArticle.class)) {
                while (itr.hasNext()) {
                    HBArticle article = itr.next();
                    fw.append(new JSONObject(article.toMongoHashMap()).toJSONString());
                    fw.append("\n");
                }
                return true;
            } catch (IOException e) {
                logger.error("生成当日的videobase全量文件失败", e);
                return false;
            }
        } else {
            return false;
        }
    }

}
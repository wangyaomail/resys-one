package xmt.resys.content.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
import xmt.resys.content.bean.mongo.HBTag;
import xmt.resys.content.dao.TagDao;

@Service
public class TagService extends BaseCRUDService<HBTag> {
    @Resource
    private TagDao tagDao;

    private ReentrantReadWriteLock exportLock = new ReentrantReadWriteLock();

    @Override
    public BaseCRUDDao<HBTag> dao() {
        return tagDao;
    }

    public Collection<HBTag> findAllTagByParent(String parent) {
        return tagDao.findAll("parent", parent);
    }

    /**
     * 从parent节点开始，获取N层的节点
     */
    public Collection<HBTag> findRecrusiveTagByParent(String parent,
                                                      int N) {
        // 1.获取第一层节点
        Map<String, Object> mongoMap = new HashMap<>();
        mongoMap.put("parent", parent);
        Collection<HBTag> roots = dao().query(mongoMap);
        // 2.递归获取root下第一层的节点
        if (roots != null && N > 0) {
            for (HBTag root : roots) {
                root.setChildren(findRecrusiveTagByParent(root.getId(), N - 1));
            }
        }
        return roots;
    }

    public boolean recursiveDelete(String id) {
        return tagDao.recursiveDelete(id, 4);
    }

    public Map<String, HBTag> getAllTree() {
        return tagDao.getAllTree();
    }

    public boolean updateId(HBTag object) {
        return tagDao.updateId(object);
    }

    /**
     * 定时将所有tag导出为一个文件
     */
    @Scheduled(cron = "0 1 * * * *")
    public boolean exportAllTag() {
        if (exportLock.writeLock().tryLock()) {
            File tagFile = new File(sysConfService.getVideobaseAllPath() + "/tag_all_"
                    + TimerService.now_to_day + ".data");
            if (!tagFile.exists()) {
                try {
                    File dir = new File(sysConfService.getVideobaseAllPath());
                    dir.mkdirs();
                    tagFile.createNewFile();
                } catch (IOException e) {
                }
            }
            try (FileWriter fw = new FileWriter(tagFile);
                    CloseableIterator<HBTag> itr = mongoTemplate.stream(new BasicQuery(new Document()),
                                                                        HBTag.class)) {
                while (itr.hasNext()) {
                    HBTag tag = itr.next();
                    fw.append(new JSONObject(tag.toMongoHashMap()).toJSONString());
                    fw.append("\n");
                }
                return true;
            } catch (IOException e) {
                logger.error("生成当日的tag全量文件失败", e);
                return false;
            }
        } else {
            return false;
        }
    }
}

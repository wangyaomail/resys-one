package xmt.resys.content.dao;

import org.springframework.stereotype.Repository;

import xmt.resys.common.dao.l4.BaseTreeLocalMongoCacheDao;
import xmt.resys.content.bean.mongo.HBArticleCategory;

/**
 * 文章分类少且小，直接全量加载到内存即可
 */
@Repository("articleCategoryDao")
public class ArticleCategoryDao extends BaseTreeLocalMongoCacheDao<HBArticleCategory> {
}

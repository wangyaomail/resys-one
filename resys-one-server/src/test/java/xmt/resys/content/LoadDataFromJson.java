package xmt.resys.content;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.alibaba.fastjson.JSONObject;

import xmt.resys.ServerBootApplication;
import xmt.resys.content.bean.enums.ArticleStateEnum;
import xmt.resys.content.bean.mongo.HBArticle;
import xmt.resys.content.bean.mongo.HBArticleCategory;
import xmt.resys.content.dao.ArticleCategoryDao;
import xmt.resys.content.dao.ArticleDao;
import xmt.resys.util.set.HBStringUtil;

/**
 * 从广电提供的文章文件中提取数据写入mongo数据库
 */
public class LoadDataFromJson {

    private static HBArticle parseFromDxnews(JSONObject dxnew,
                                             ArticleCategoryDao articleCategoryDao) {
        HBArticle article = new HBArticle();
        article.setId(dxnew.getString("articleId"));
        article.setTitle(dxnew.getString("articleTitle"));
        article.setContent(dxnew.getString("contentBody"));
        article.setState(ArticleStateEnum.DRAFT.getIndex());
        article.setCreateTime(dxnew.getDate("createTime"));
        article.setPublishTime(dxnew.getDate("publishTime"));
        {
            ArrayList<String> cateArrayList = new ArrayList<String>();
            String channelName = "导航-" + dxnew.getString("channelName");
            if (channelName != null) {
                HBArticleCategory category = articleCategoryDao.findOne(channelName);
                if (category == null) {
                    category = new HBArticleCategory();
                    category.setId(channelName);
                    category.setParent("导航");
                    articleCategoryDao.insert(category);
                }
                cateArrayList.add(channelName);
            }
            cateArrayList.add("来源-测试");
            article.setCategorys(cateArrayList);
        }
        article.setSource(dxnew.getString("articleOrigin"));
        article.setSourceLink(dxnew.getString("linkTo"));
        article.setArticleAuthor(dxnew.getString("articleAuthor"));
        article.prepareHBBean();
        return article;
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(ServerBootApplication.class,
                                                                      args);
        ArticleDao articleDao = applicationContext.getBean(ArticleDao.class);
        ArticleCategoryDao articleCategoryDao = applicationContext.getBean(ArticleCategoryDao.class);
        System.out.println(articleDao.count());
        // 遍历本地目录
        File dxnews = new File("D:\\新媒体中心\\2019\\推荐系统\\dxnews");
        if (dxnews.exists() && dxnews.isDirectory()) {
            File[] newsList = dxnews.listFiles();
            for (int i = 0; i < newsList.length; i++) {
                if (newsList[i].isFile()) {
                    try {
                        FileInputStream in = new FileInputStream(newsList[i]);
                        byte[] buffer = new byte[in.available()];
                        in.read(buffer);
                        in.close();
                        String str = new String(buffer, "utf8");
                        if (HBStringUtil.isNotBlank(str)) {
                            JSONObject jsonObject = JSONObject.parseObject(str);
                            String articleId = jsonObject.getString("articleId");
                            if (HBStringUtil.isNotBlank(articleId)) {
                                // System.out.println(articleId);
                                HBArticle article = parseFromDxnews(jsonObject, articleCategoryDao);
                                articleDao.insert(article);
                            }
                        } else {
                            System.err.println(newsList[i].getName() + "文件是空的");
                        }
                    } catch (Exception e) {
                        System.out.println(newsList[i].getName() + " 读取失败");
                        e.printStackTrace();
                    }
                }
            }
        }
        System.exit(0);
    }
}

package xmt.resys;

import org.apache.hadoop.conf.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import xmt.resys.videobase.ArticleHBaseDao;

public class TestHBase {
    public static void main(String[] args) {
        try {
            System.setProperty("hadoop.home.dir", "D:\\assistlibs\\hadoop\\winutils\\hadoop-3.2.1");
            Configuration conf = new Configuration();
            conf.set("hadoop.home.dir", "D:\\assistlibs\\hadoop\\winutils\\hadoop-3.2.1");
            ApplicationContext applicationContext = SpringApplication.run(StreamBootApplication.class,
                                                                          args);
            ArticleHBaseDao ulcd = applicationContext.getBean(ArticleHBaseDao.class);
            if (!ulcd.existsTable()) {
                ulcd.createTable(1);
            }
            ulcd.put("a", "b", "c");
            System.out.println(ulcd.get("a", "b"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

}

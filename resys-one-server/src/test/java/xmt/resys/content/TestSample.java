package xmt.resys.content;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import xmt.resys.ServerBootApplication;
import xmt.resys.resys.dao.RecArticleUserCouchbaseDao;
import xmt.resys.resys.service.ResysService;

public class TestSample {
    public static void main(String[] args) {
        try {
            ApplicationContext applicationContext = SpringApplication.run(ServerBootApplication.class,
                                                                          args);
            ResysService resysService = applicationContext.getBean(ResysService.class);
            resysService.randomInsertFeed("12479083317097", 100);
            long startTime = System.currentTimeMillis();
            System.out.println(resysService.getUserRecList("12479083317097", 10));
            System.out.println(resysService.getUserRecList("12479083317097", 20));
            System.out.println(resysService.getUserRecList("12479083317097", 20));
            System.out.println(resysService.getUserRecList("12479083317097", 30));
            System.out.println(resysService.getUserRecList("12479083317097", 10));
            System.out.println(resysService.getUserRecList("12479083317097", 20));
            System.out.println(System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main3(String[] args) {
        try {
            ApplicationContext applicationContext = SpringApplication.run(ServerBootApplication.class,
                                                                          args);
            ResysService resysService = applicationContext.getBean(ResysService.class);
            resysService.randomInsertFeed("12479083317097", 10);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main2(String[] args) {
        try {
            ApplicationContext applicationContext = SpringApplication.run(ServerBootApplication.class,
                                                                          args);
            RecArticleUserCouchbaseDao dao = applicationContext.getBean(RecArticleUserCouchbaseDao.class);
            System.out.println(dao.findOne("123"));
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

}

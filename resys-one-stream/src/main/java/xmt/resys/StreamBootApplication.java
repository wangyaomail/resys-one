package xmt.resys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAutoConfiguration(exclude = { MongoAutoConfiguration.class,
                                     MongoDataAutoConfiguration.class })
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = { "xmt.resys" })
public class StreamBootApplication {
    public static ApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(StreamBootApplication.class, args);
        System.out.println("系统【" + applicationContext.getDisplayName() + "】初始化完毕");
    }
}

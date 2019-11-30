package xmt.resys.content;

import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.alibaba.fastjson.JSONObject;

import xmt.resys.ServerBootApplication;
import xmt.resys.content.dao.ArticleChangeSendDao;

/**
 * 测试写入activemq和从activemq中读出数据
 */
public class TestActivemq {

    @SuppressWarnings("serial")
    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = SpringApplication.run(ServerBootApplication.class,
                                                                      args);
        ArticleChangeSendDao sendDao = applicationContext.getBean(ArticleChangeSendDao.class);
        JSONObject obj = new JSONObject();
        obj.put("a", "123");
        obj.put("b", "456");
        obj.put("c", "789");
        obj.put("list", new ArrayList<String>() {
            {
                this.add("x");
                this.add("y");
                this.add("z");
            }
        });
        for (int i = 0; i < 100; i++) {
            System.out.println("发送" + obj);
            sendDao.sendMessage(obj.toJSONString().getBytes("UTF-8"));
        }
    }
}

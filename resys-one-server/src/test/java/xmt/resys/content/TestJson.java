package xmt.resys.content;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.couchbase.client.java.document.json.JsonObject;

import lombok.Data;
import xmt.resys.common.bean.mongo.BaseIdBean;
import xmt.resys.resys.bean.kafka.UserListClickRecord;
import xmt.resys.util.set.HBCollectionUtil;

public class TestJson {
    public static void main(String[] args) {
        UserListClickRecord record = new UserListClickRecord();
        record.setId("a");
        record.setList(new ArrayList<String>() {
            private static final long serialVersionUID = -8975907243846648533L;

            {
                add("1");
                add("2");
            }
        });
        System.out.println(record);
        System.out.println("----------");
        String str = record.toJSONString();
        Record2 r2 = Record2.fromJSONString(str);
        System.out.println(r2);
    }

    public static void main2(String[] args) {
        main(new String[2]);
    }

    public static void main4(String[] args) {
        JsonObject json = JsonObject.create();
        json.put("a", 2);
        json.put("b", "bbb");
        json.put("c", HBCollectionUtil.arrayToList("a", "b", "c"));
        System.out.println(json);
        System.out.println(json.get("c"));
        System.out.println(json.getString("c"));
    }

    public static void main3(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        System.out.println(list.subList(1, 10));
    }

    @Data
    static class Record2 implements BaseIdBean {
        private String id;
        private String userid;
        private String orderid; // 推荐订单id
        private String click; // 用户点击的新闻id
        private List<String> list; // 本次给用户所展示的新闻id
        private long time; // 用户点击的时间

        public String toJSONString() {
            return JSON.toJSONString(this);
        }

        public static Record2 fromJSONString(String str) {
            return JSON.parseObject(str, Record2.class);
        }

        public String toString() {
            return toJSONString();
        }

    }

}

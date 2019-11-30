package xmt.resys.userlog;

import java.util.List;

import com.alibaba.fastjson.JSON;

import lombok.Data;
import xmt.resys.common.bean.mongo.BaseIdBean;

/**
 * 用户点击列表所产生的点击事件反馈
 */
@Data
public class UserListClickRecord implements BaseIdBean {
    private String id;
    private String userid;
    private String recid; // 推荐订单id
    private String click; // 用户点击的新闻id
    private List<String> list; // 本次给用户所展示的新闻id
    private Long clickTime; // 用户点击的时间

    public String toJSONString() {
        return JSON.toJSONString(this);
    }

    public static UserListClickRecord fromJSONString(String str) {
        return JSON.parseObject(str, UserListClickRecord.class);
    }

    public String toString() {
        return toJSONString();
    }

}

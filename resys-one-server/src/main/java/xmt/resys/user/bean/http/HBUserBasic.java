package xmt.resys.user.bean.http;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * 在一些基本的场合，提供用户最基础的信息返回
 */
@Data
@Document(collection = "hb_users")
public class HBUserBasic implements Serializable {
    private static final long serialVersionUID = 1926681181232419176L;
    @Id
    private String id; // 用户id
    private String userName; // 用户登录名

    public HBUserBasic() {}

    public HBUserBasic(String id) {
        setId(id);
    }
}

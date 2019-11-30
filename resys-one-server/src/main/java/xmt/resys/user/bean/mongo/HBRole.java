package xmt.resys.user.bean.mongo;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.mongo.BaseMgBean;

/**
 * 我们这里使用两重验证，如果是配了jwtRole的，使用jwtRole进行验证，否则我们自己来设置验证方式
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_roles")
public class HBRole extends BaseMgBean<HBRole> implements Serializable {
    private static final long serialVersionUID = 1315568341117606585L;
    @Id
    private String id; // id
    private String name; // 展示名称
    private String jwt;
    @Indexed
    private List<String> modules; // 允许通过的url
}

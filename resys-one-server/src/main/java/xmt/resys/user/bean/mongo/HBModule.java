package xmt.resys.user.bean.mongo;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xmt.resys.common.bean.mongo.BaseMgBean;
import xmt.resys.web.bean.auth.HBAuthURL;

/**
 * 系统模块
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "hb_modules")
public class HBModule extends BaseMgBean<HBModule> implements Serializable {
    private static final long serialVersionUID = -1580843791657747151L;
    @Id
    private String id; // id
    private String name; // 展示名称
    private Boolean record; // 是否记录访问情况
    private String category; // 模块分类
    private List<HBAuthURL> urls; // url-method(GET,POST,PUT,DELETE)，后台不需要做单独的url的库，这个复合对象强制完整存储后台
    private String feature; // 该模块的功能

    @Override
    public void prepareHBBean() {
        // urls = urls == null ? new ArrayList<>() : null;
        record = record == null ? false : true;
    }
}

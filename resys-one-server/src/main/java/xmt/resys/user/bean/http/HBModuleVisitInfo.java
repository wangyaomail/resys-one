package xmt.resys.user.bean.http;

import org.springframework.data.annotation.Transient;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;
import xmt.resys.user.bean.mongo.HBModule;

/**
 * 系统模块访问情况，但单独保存数据库，和系统信息保存到一起
 */
@Data
public class HBModuleVisitInfo {
    private String id; // 和HBModule的id对应
    private String name; // 和HBModule的name对应
    private Integer count; // 访问数量
    @Transient
    @JSONField(serialize = false)
    private HBModule srcModule; // 来源的module

    public HBModuleVisitInfo fromHBModule(HBModule module) {
        this.id = module.getId();
        this.name = module.getName();
        this.count = this.count == null ? 0 : this.count;
        this.srcModule = module;
        return this;
    }
}

package xmt.resys.common.bean.mongo;

import java.util.Collection;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类基础类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseTreeMgBean<T> extends BaseMgBean<T> {
    @Indexed
    private String parent;
    @Transient
    private Collection<T> children;
    @Transient
    private String oldId; // 用来更改id
}

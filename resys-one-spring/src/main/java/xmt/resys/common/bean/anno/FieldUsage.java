package xmt.resys.common.bean.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段策略
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.ANNOTATION_TYPE })
public @interface FieldUsage {
    /**
     * 是否该字段可以保存到数据库，默认可以保存
     */
    boolean save() default true;

    /**
     * 该字段是否可以发送给前端用，默认可以
     */
    boolean send() default true;
}

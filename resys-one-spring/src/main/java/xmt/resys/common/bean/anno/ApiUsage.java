package xmt.resys.common.bean.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法谁能调用
 * @INCLUDE Controller、Dao、Service哪个可以
 * @INCLUDE 是否可以跨模块
 * @INCLUDE 是否可以被反射调用
 * @INCLUDE 是否可以按照名称来调用
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface ApiUsage {
    /**
     * 该方法可否被controller调用，默认可以
     */
    boolean controller() default true;

    /**
     * 该方法可否被dao调用，默认可以
     */
    boolean dao() default true;

    /**
     * 该方法可否被service调用，默认可以
     */
    boolean service() default true;

    /**
     * 该方法可否跨模块调用，默认可以
     */
    boolean crossmodule() default true;

    /**
     * 是否可以被反射调用，默认可以
     */
    boolean byreflect() default true;

    /**
     * 是否可以按照名称调用，默认可以
     */
    boolean byname() default true;
}

package xmt.resys.common.bean.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将这个调用的方法注册到对象池中
 * @INFO 如果用在类上，则意味着这个类能够使用的内部的declare为true的方法
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE, ElementType.METHOD })
public @interface RegisterMethod {
    /**
     * 如果配置了name则以name作为key，否则以方法名作为key
     */
    String name() default "";

    /**
     * 是否需要在标题声明该应用，默认不需要，对于特别实现的方法则需要
     * @value=true：必须在类头上声明methods方法，否则不可调用
     * @value=false：不需要在类头做methods的声明也可以被前端调用
     */
    boolean declare() default false;

    /**
     * 是否能够被声明的方法
     */
    String[] methods() default {};
}

package io.izzel.taboolib.module.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author sky
 * @Since 2018-08-22 13:41
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TListener {

    /**
     * 注册时执行的方法名
     */
    String register() default "";

    /**
     * 注销时执行的方法名
     */
    String cancel() default "";

    /**
     * 注册时判断的方法名，需返回布尔值
     */
    String condition() default "";

    /**
     * 注册前判断依赖插件
     */
    String[] depend() default "";

    /**
     * 注册前判断依赖版本
     */
    String version() default ">0";

}
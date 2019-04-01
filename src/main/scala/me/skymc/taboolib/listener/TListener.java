package me.skymc.taboolib.listener;

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
     * 注册时执行方法
     *
     * @return 方法名
     */
    String register() default "";

    /**
     * 注销时执行方法
     *
     * @return 方法名
     */
    String cancel() default "";

    /**
     * 注册时判断条件
     *
     * @return 方法名
     */
    String condition() default "";

    /**
     * 注册前判断依赖插件
     *
     * @return 依赖插件
     */
    String[] depend() default "";

}
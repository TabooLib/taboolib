package io.izzel.taboolib.module.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author 坏黑
 * @Since 2018-12-15 15:07
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TSchedule {

    int delay() default 0;
    int period() default -1;

    boolean async() default false;

}

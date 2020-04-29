package io.izzel.taboolib.module.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author sky
 * @Since 2018-10-05 12:11
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TInject {

    String[] value() default {};

    String asm() default "";

    String load() default "";

    String init() default "";

    String active() default "";

    String cancel() default "";

    String reload() default "";

    String locale() default "";

    State state() default State.NONE;

    boolean autoMigrate() default false;

    enum State {

        LOADING, STARTING, ACTIVATED, NONE
    }
}
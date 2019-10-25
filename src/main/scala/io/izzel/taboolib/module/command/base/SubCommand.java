package io.izzel.taboolib.module.command.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author sky
 * @Since 2018-05-09 22:38
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommand {

    double priority() default 0;

    String permission() default "";

    String description() default "";

    String[] aliases() default {};

    String[] arguments() default {};

    boolean ignoredLabel() default true;

    boolean requiredPlayer() default false;

    boolean hideInHelp() default false;

    CommandType type() default CommandType.ALL;
}

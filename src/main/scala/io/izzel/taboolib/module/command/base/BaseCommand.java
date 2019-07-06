package io.izzel.taboolib.module.command.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author sky
 * @Since 2018-08-23 20:34
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseCommand {

    String name();

    String permission() default "";

    String permissionMessage() default "";

    String description() default "";

    String usage() default "";

    String[] aliases() default {};
}
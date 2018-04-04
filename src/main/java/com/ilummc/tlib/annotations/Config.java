package com.ilummc.tlib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {

    String name() default "config.yml";

    boolean fromJar() default true;

    boolean saveOnExit() default false;

    boolean readOnly() default true;

    boolean fixUnicode() default true;

    String charset() default "UTF-8";

    boolean listenChanges() default false;

}

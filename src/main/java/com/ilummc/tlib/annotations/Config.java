package com.ilummc.tlib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {

    String name() default "config.yml";

    boolean fromJar() default false;

    boolean saveOnExit() default false;

    String charset() default "UTF-8";

    boolean listenChanges() default false;

    int excludeModifiers() default Modifier.STATIC | Modifier.TRANSIENT;

}

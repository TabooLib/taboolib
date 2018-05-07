package com.ilummc.tlib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

import com.ilummc.tlib.util.Ref;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TConfig {

    String name() default "config.yml";

    boolean fromJar() default false;

    boolean saveOnExit() default false;

    boolean readOnly() default true;

    String charset() default "UTF-8";

    boolean listenChanges() default false;

    int excludeModifiers() default Modifier.STATIC | Modifier.TRANSIENT | Ref.ACC_SYNTHETIC | Ref.ACC_BRIDGE;

}

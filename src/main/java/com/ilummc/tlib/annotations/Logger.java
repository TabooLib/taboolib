package com.ilummc.tlib.annotations;

import com.ilummc.tlib.util.TLogger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Logger {

    String value() default "[{0}] {1}";

    int level() default TLogger.INFO;

}

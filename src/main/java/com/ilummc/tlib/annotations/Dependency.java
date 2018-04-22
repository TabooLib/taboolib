package com.ilummc.tlib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ilummc.tlib.dependency.TDependency;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Dependencies.class)
public @interface Dependency {

    enum Type {PLUGIN, LIBRARY}

    Type type();

    String plugin() default "";

    String maven() default "";

    String mavenRepo() default TDependency.MAVEN_REPO;

    String url() default "";

}

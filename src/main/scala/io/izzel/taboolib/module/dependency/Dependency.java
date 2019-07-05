package io.izzel.taboolib.module.dependency;

import java.lang.annotation.*;

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

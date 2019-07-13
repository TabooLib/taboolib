package io.izzel.taboolib.module.dependency;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Dependencies.class)
public @interface Dependency {

    String maven() default "";

    String mavenRepo() default TDependency.MAVEN_REPO;

    String url() default "";

}

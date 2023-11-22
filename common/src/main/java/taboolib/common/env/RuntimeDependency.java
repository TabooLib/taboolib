package taboolib.common.env;

import java.lang.annotation.*;

/**
 * 使用 ! 前缀来避免在编译过程中被 taboolib-gradle-plugin 或 shadowJar 二次重定向。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RuntimeDependencies.class)
public @interface RuntimeDependency {

    String value();

    String test() default "";

    // Leave empty to use default repository
    // https://maven.aliyun.com/repository/central
    String repository() default "";

    boolean transitive() default true;

    boolean ignoreOptional() default true;

    boolean ignoreException() default false;

    DependencyScope[] scopes() default {DependencyScope.RUNTIME, DependencyScope.COMPILE};

    String[] relocate() default {};

    boolean initiative() default false;
    
    boolean isolated() default false;
    
}
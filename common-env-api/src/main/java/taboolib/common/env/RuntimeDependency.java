package taboolib.common.env;

import java.lang.annotation.*;

/**
 * 使用 ! 前缀来避免在编译过程中被 taboolib-gradle-plugin 或 shadowJar 二次重定向。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RuntimeDependencies.class)
public @interface RuntimeDependency {

    /** 依赖地址 */
    String value();

    /** 测试类 */
    String test() default "";

    /**
     * 仓库地址，留空默认使用 <a href="https://maven.aliyun.com/repository/central">阿里云中央仓库</a>
     */
    String repository() default "";

    /** 是否进行依赖传递 */
    boolean transitive() default true;

    /** 忽略可选 */
    boolean ignoreOptional() default true;

    /** 忽略异常 */
    boolean ignoreException() default false;

    /** 依赖范围 */
    DependencyScope[] scopes() default {DependencyScope.RUNTIME, DependencyScope.COMPILE};

    /** 依赖重定向 */
    String[] relocate() default {};

    /**
     * 是否隔离，需要启用 "enable-isolated-classloader" 选项，隔离后有严格的开发规范，请勿在不了解的情况下使用
     */
    boolean isolated() default false;
}
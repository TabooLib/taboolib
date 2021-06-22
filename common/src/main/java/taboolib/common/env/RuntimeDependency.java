package taboolib.common.env;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RuntimeDependencies.class)
public @interface RuntimeDependency {

    String value();

    String test() default "";

    String repository() default "https://maven.aliyun.com/repository/central";

    boolean ignoreOptional() default true;

    DependencyScope[] scopes() default {DependencyScope.RUNTIME};
}
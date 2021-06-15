package taboolib.common.env.runtime;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RuntimeDependencies.class)
public @interface RuntimeDependency {

    String group();

    String id();

    String version();

    String hash();

    String repository() default "https://maven.aliyun.com/repository/central";

}

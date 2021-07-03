package taboolib.common.env;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RuntimeResources.class)
public @interface RuntimeResource {

    String value();

    String hash();

    String name() default "";
}
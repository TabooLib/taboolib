package taboolib.module.dependency;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RuntimeNames.class)
public @interface RuntimeName {

    String group();

    String name();

}

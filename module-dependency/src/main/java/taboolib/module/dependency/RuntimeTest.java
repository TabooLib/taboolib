package taboolib.module.dependency;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RuntimeTests.class)
public @interface RuntimeTest {

    String group();

    String[] path();

}

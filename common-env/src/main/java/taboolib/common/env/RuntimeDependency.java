package taboolib.common.env;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RuntimeDependencies.class)
public @interface RuntimeDependency {

    /**
     * 依赖地址
     */
    String value();

    /**
     * 测试类
     * <p>
     * <code>
     * test = "!org.bukkit.Bukkit" // 前面带个感叹号避免在编译时重定向
     * </code>
     */
    String test() default "";

    /**
     * 仓库地址，留空默认使用 <a href="https://maven.aliyun.com/repository/central">阿里云中央仓库</a>
     */
    String repository() default "";

    /**
     * 是否进行依赖传递
     */
    boolean transitive() default true;

    /**
     * 忽略可选依赖
     */
    boolean ignoreOptional() default true;

    /**
     * 忽略加载异常
     */
    boolean ignoreException() default false;

    /**
     * 依赖范围
     */
    DependencyScope[] scopes() default {DependencyScope.RUNTIME, DependencyScope.COMPILE};

    /**
     * 依赖重定向
     * <p>
     * <code>
     * relocate = ["!taboolib.", "!taboolib610."] // 同 test 参数
     * </code>
     */
    String[] relocate() default {};

    /**
     * 是否外部库（不会被扫到）
     */
    boolean external() default true;
}
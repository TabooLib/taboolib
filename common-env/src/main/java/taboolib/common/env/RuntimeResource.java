package taboolib.common.env;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RuntimeResources.class)
public @interface RuntimeResource {

    /**
     * 资源地址
     */
    String value();

    /**
     * 实际资源的哈希值
     * 采用 SHA-1 算法
     */
    String hash();

    /**
     * 资源名称
     * 留空使用哈希值作为名称
     */
    String name() default "";

    /**
     * 标签（标识用）
     */
    String tag() default "";

    /**
     * 是否为压缩文件，启用后从 value() + ".zip" 下载文件，
     * 无论是否启用，哈希值判断都依据 ".zip" 内的实际资源，而非 ".zip" 本身
     */
    boolean zip() default false;

}
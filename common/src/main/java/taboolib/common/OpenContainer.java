package taboolib.common;

import org.jetbrains.annotations.NotNull;

/**
 * TabooLib
 * taboolib.common.OpenContainer
 *
 * @author sky
 * @since 2021/7/3 1:39 上午
 */
public interface OpenContainer {

    /**
     * 插件名字
     *
     * @return {@link String}
     */
    String getName();

    /**
     * 注册开放接口
     *
     * @param name 名字
     * @param args 参数
     */
    @NotNull
    OpenResult call(String name, @NotNull Object[] args);
}

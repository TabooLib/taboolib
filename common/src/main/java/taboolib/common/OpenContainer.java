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

    String getName();

    void register(@NotNull Object any);

    void unregister(@NotNull Object any);
}

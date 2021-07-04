package taboolib.common;

import org.jetbrains.annotations.NotNull;

/**
 * TabooLib
 * taboolib.common.OpenReceiver
 *
 * @author sky
 * @since 2021/7/4 8:30 下午
 */
public interface OpenReceiver {

    boolean register(@NotNull String name, @NotNull byte[] any, @NotNull String[] args);

    boolean unregister(@NotNull String name, @NotNull byte[] any, @NotNull String[] args);
}

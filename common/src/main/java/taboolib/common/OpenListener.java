package taboolib.common;

import org.jetbrains.annotations.NotNull;

/**
 * TabooLib
 * taboolib.common.OpenReceiver
 *
 * @author sky
 * @since 2021/7/4 8:30 下午
 */
public interface OpenListener {

    @NotNull
    OpenResult call(@NotNull String name, @NotNull Object[] data);
}

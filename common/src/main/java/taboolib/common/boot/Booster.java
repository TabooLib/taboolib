package taboolib.common.boot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.platform.Platform;

/**
 * TabooLib
 * taboolib.common.boot.Booster
 *
 * @author 坏黑
 * @since 2022/1/25 2:13 AM
 */
public interface Booster {

    Monitor getMonitor();

    Platform getPlatform();

    void proceed(@NotNull LifeCycle lifeCycle);

    void proceed(@NotNull LifeCycle lifeCycle, @Nullable Platform platform);

    void join(@NotNull LifeCycle lifeCycle, @NotNull Runnable runnable);
}

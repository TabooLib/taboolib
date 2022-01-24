package taboolib.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.TabooLib;
import taboolib.common.boot.Booster;
import taboolib.common.boot.Monitor;
import taboolib.common.platform.Platform;

/**
 * TabooLib
 * taboolib.internal.SimpleBooster
 *
 * @author 坏黑
 * @since 2022/1/25 2:33 AM
 */
public class SimpleBooster implements Booster {

    @Override
    public Monitor getMonitor() {
        return null;
    }

    @Override
    public Platform getPlatform() {
        return null;
    }

    @Override
    public void proceed(@NotNull LifeCycle lifeCycle) {

    }

    @Override
    public void proceed(@NotNull LifeCycle lifeCycle, @Nullable Platform platform) {

    }

    @Override
    public void join(@NotNull LifeCycle lifeCycle, @NotNull Runnable runnable) {

    }
}

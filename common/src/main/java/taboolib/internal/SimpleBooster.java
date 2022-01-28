package taboolib.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.boot.Booster;
import taboolib.common.boot.Environments;
import taboolib.common.boot.Mechanism;
import taboolib.common.boot.Monitor;
import taboolib.common.env.RuntimeEnv;
import taboolib.common.inject.InjectorHandler;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformFactory;

import java.util.*;

/**
 * TabooLib
 * taboolib.internal.SimpleBooster
 *
 * @author 坏黑
 * @since 2022/1/25 2:33 AM
 */
public class SimpleBooster implements Booster {

    public static final SimpleBooster INSTANCE = new SimpleBooster();

    final Map<LifeCycle, List<Runnable>> postpone = new EnumMap<>(LifeCycle.class);
    final SimpleMonitor monitor = new SimpleMonitor();

    Platform runningPlatform = Platform.APPLICATION;
    boolean initiation = false;

    @NotNull
    @Override
    public Monitor getMonitor() {
        return monitor;
    }

    @NotNull
    @Override
    public Platform getPlatform() {
        return runningPlatform;
    }

    @Override
    public void proceed(@NotNull LifeCycle lifeCycle) {
        proceed(lifeCycle, null);
    }

    @Override
    public void proceed(@NotNull LifeCycle lifeCycle, @Nullable Platform platform) {
        if (monitor.isShutdown()) {
            return;
        }
        if (platform != null) {
            runningPlatform = platform;
        }
        if (postpone.containsKey(lifeCycle)) {
            postpone.get(lifeCycle).forEach(i -> {
                try {
                    i.run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
        }
        switch (lifeCycle) {
            case CONST:
                if (setupKotlin()) {
                    preInitiation();
                }
                break;
            case INIT:
                if (initiation) {
                    InjectorHandler.INSTANCE.inject(LifeCycle.INIT);
                }
                break;
            case LOAD:
                if (!initiation) {
                    if (Environments.isKotlin()) {
                        preInitiation();
                        InjectorHandler.INSTANCE.inject(LifeCycle.INIT);
                    } else {
                        monitor.setShutdown(true);
                        throw new RuntimeException("Runtime environment setup failed, please feedback!");
                    }
                }
                InjectorHandler.INSTANCE.inject(LifeCycle.LOAD);
                break;
            case ENABLE:
                InjectorHandler.INSTANCE.inject(LifeCycle.ENABLE);
                break;
            case ACTIVE:
                InjectorHandler.INSTANCE.inject(LifeCycle.ACTIVE);
                break;
            case DISABLE:
                InjectorHandler.INSTANCE.inject(LifeCycle.DISABLE);
                Mechanism.shutdown(PlatformFactory.INSTANCE);
                break;
        }
    }

    @Override
    public void join(@NotNull LifeCycle lifeCycle, @NotNull Runnable runnable) {
        postpone.computeIfAbsent(lifeCycle, i -> new ArrayList<>()).add(runnable);
    }

    void preInitiation() {
        initiation = true;
        Mechanism.startup(PlatformFactory.INSTANCE);
        InjectorHandler.INSTANCE.inject(LifeCycle.CONST);
    }

    boolean setupKotlin() {
        if (Environments.isKotlin()) {
            return true;
        }
        try {
            // RuntimeEnv 类可能会被插件强制移除导致 NoClassDefFoundError 异常，这是正常的
            Mechanism.startup(RuntimeEnv.INSTANCE);
        } catch (NoClassDefFoundError | ClassCastException ignored) {
        }
        return Environments.isKotlin();
    }
}

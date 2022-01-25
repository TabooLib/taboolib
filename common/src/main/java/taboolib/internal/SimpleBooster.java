package taboolib.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.LifeCycle;
import taboolib.common.boot.Booster;
import taboolib.common.boot.Environments;
import taboolib.common.boot.Monitor;
import taboolib.common.env.RuntimeEnv;
import taboolib.common.inject.InjectorFactory;
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

    SimpleBooster() {
    }

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
        this.runningPlatform = platform;
        this.postpone.get(lifeCycle).forEach(i -> {
            try {
                i.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        switch (lifeCycle) {
            case CONST:
                if (setupKotlin()) {
                    preInitiation();
                }
                break;
            case INIT:
                if (initiation) {
                    InjectorFactory.INSTANCE.inject(LifeCycle.INIT);
                }
                break;
            case LOAD:
                if (!initiation) {
                    if (Environments.isKotlin()) {
                        preInitiation();
                        InjectorFactory.INSTANCE.inject(LifeCycle.INIT);
                    } else {
                        monitor.setShutdown(true);
                        throw new RuntimeException("Runtime environment setup failed, please feedback!");
                    }
                }
                InjectorFactory.INSTANCE.inject(LifeCycle.LOAD);
                break;
            case ENABLE:
                InjectorFactory.INSTANCE.inject(LifeCycle.ENABLE);
                break;
            case ACTIVE:
                InjectorFactory.INSTANCE.inject(LifeCycle.ACTIVE);
                break;
            case DISABLE:
                InjectorFactory.INSTANCE.inject(LifeCycle.DISABLE);
                PlatformFactory.INSTANCE.cancel();
                break;
        }
    }

    @Override
    public void join(@NotNull LifeCycle lifeCycle, @NotNull Runnable runnable) {
        postpone.get(lifeCycle).add(runnable);
    }

    void preInitiation() {
        initiation = true;
        PlatformFactory.INSTANCE.init();
        InjectorFactory.INSTANCE.inject(LifeCycle.CONST);
    }

    boolean setupKotlin() {
        if (Environments.isKotlin()) {
            return true;
        }
        try {
            // RuntimeEnv 类可能会被插件强制移除导致 NoClassDefFoundError 异常，这是正常的
            RuntimeEnv.ENV.setup();
        } catch (NoClassDefFoundError ignored) {
        }
        return Environments.isKotlin();
    }
}

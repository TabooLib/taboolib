package taboolib.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.env.RuntimeDependency;
import taboolib.common.env.RuntimeEnv;
import taboolib.common.inject.InjectorFactory;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformFactory;
import taboolib.internal.InjectorHandlerImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TabooLib
 * taboolib.common.TabooLibCommon
 *
 * @author sky
 * @since 2021/6/15 2:45 下午
 */
@RuntimeDependency(value = "!com.google.code.gson:gson:2.8.7", test = "!com.google.gson.JsonElement")
public class TabooLibCommon {

    private static Platform platform = Platform.APPLICATION;

    private static boolean stopped = false;

    private static boolean sysoutCatcherFound = false;

    private static boolean isInitiation = false;

    private static final Map<LifeCycle, List<Runnable>> postponeTask = new HashMap<>();

    static {
        try {
            // 无法理解 paper 的伞兵行为
            Class.forName("io.papermc.paper.logging.SysoutCatcher");
            sysoutCatcherFound = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    /**
     * 用于测试的快速启动方法
     * 会按顺序触发 CONST、INIT、LOAD、ENABLE 生命周期
     */
    public static void startNow() {
        lifeCycle(LifeCycle.CONST);
        lifeCycle(LifeCycle.INIT);
        lifeCycle(LifeCycle.LOAD);
        lifeCycle(LifeCycle.ENABLE);
        lifeCycle(LifeCycle.ACTIVE);
    }

    /**
     * 用于测试的快速注销方法
     * 会触发 DISABLE 生命周期
     */
    public static void disableNow() {
        lifeCycle(LifeCycle.DISABLE);
    }

    /**
     * 推迟任务到指定 LifeStyle 执行
     */
    public static void postpone(LifeCycle lifeCycle, Runnable runnable) {
        postponeTask.computeIfAbsent(lifeCycle, list -> new ArrayList<>()).add(runnable);
    }

    /**
     * 触发生命周期
     */
    public static void lifeCycle(LifeCycle lifeCycle) {
        lifeCycle(lifeCycle, null);
    }

    /**
     * 生命周期
     * 依赖于任意平台的生命周期的启动或卸载方法
     *
     * @param lifeCycle 生命周期
     */
    public static void lifeCycle(LifeCycle lifeCycle, @Nullable Platform platform) {
        if (stopped) {
            return;
        }
        // 运行平台由第一次 lifeCycle 的平台决定
        if (platform != null) {
            TabooLibCommon.platform = platform;
        }
        // 执行推迟任务
        postponeTask.forEach((cycle, list) -> {
            if (cycle == lifeCycle) {
                list.forEach((runnable) -> {
                    try {
                        runnable.run();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                });
                postponeTask.remove(cycle);
            }
        });
        // 生命周期阶段
        switch (lifeCycle) {
            case CONST:
                try {
                    RuntimeEnv.ENV.setup();
                } catch (NoClassDefFoundError ignored) {
                }
                // 如果 Kotlin 环境就绪
                if (isKotlinEnvironment()) {
                    preInitiation();
                }
                break;
            case INIT:
                if (isInitiation) {
                    InjectorFactory.INSTANCE.injectAll(LifeCycle.INIT);
                }
                break;
            case LOAD:
                if (!isInitiation) {
                    if (isKotlinEnvironment()) {
                        preInitiation();
                        InjectorFactory.INSTANCE.injectAll(LifeCycle.INIT);
                    } else {
                        stopped = true;
                        throw new RuntimeException("Runtime environment setup failed, please feedback!");
                    }
                }
                InjectorFactory.INSTANCE.injectAll(LifeCycle.LOAD);
                break;
            case ENABLE:
                InjectorFactory.INSTANCE.injectAll(LifeCycle.ENABLE);
                break;
            case ACTIVE:
                InjectorFactory.INSTANCE.injectAll(LifeCycle.ACTIVE);
                break;
            case DISABLE:
                InjectorFactory.INSTANCE.injectAll(LifeCycle.DISABLE);
                PlatformFactory.INSTANCE.cancel();
                break;
        }
    }

    /**
     * 预初始化过程，初始化 PlatformFactory 并注册一些内部实现，同时注入 CONST 生命周期
     */
    private static void preInitiation() {
        isInitiation = true;
        PlatformFactory.INSTANCE.init();
        InjectorFactory.INSTANCE.registerHandler(new InjectorHandlerImpl());
        InjectorFactory.INSTANCE.injectAll(LifeCycle.CONST);
    }

    /**
     * 当前是否存在 Kotlin 运行环境
     */
    public static boolean isKotlinEnvironment() {
        try {
            Class.forName("kotlin.Lazy", false, TabooLibCommon.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * 当前运行平台
     */
    @NotNull
    public static Platform getRunningPlatform() {
        return platform;
    }

    /**
     * 是否停止 TabooLib 及插件加载流程
     */
    public static boolean isStopped() {
        return stopped;
    }

    /**
     * 是否正在运行
     */
    public static boolean isRunning() {
        return !stopped;
    }

    /**
     * 停止 TabooLib 及插件加载流程
     */
    public static void setStopped(boolean value) {
        stopped = value;
    }

    /**
     * 是否被 Paper 核心拦截控制台打印
     */
    public static boolean isSysoutCatcherFound() {
        return sysoutCatcherFound;
    }
}

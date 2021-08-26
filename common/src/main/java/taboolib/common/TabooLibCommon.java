package taboolib.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.env.RuntimeDependency;
import taboolib.common.env.RuntimeEnv;
import taboolib.common.inject.RuntimeInjector;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformFactory;

/**
 * TabooLib
 * taboolib.common.TabooLibCommon
 *
 * @author sky
 * @since 2021/6/15 2:45 下午
 */
@RuntimeDependency(value = "!com.google.code.gson:gson:2.8.7", test = "!com.google.gson.JsonElement")
public class TabooLibCommon {

    /**
     * 保存最近一次初始化的运行环境
     */
    private static Platform platform = Platform.UNKNOWN;

    /**
     * 是否停止加载
     */
    private static boolean stopped = false;

    /**
     * 是否被 Paper 核心拦截控制台打印
     */
    private static boolean sysoutCatcherFound = false;

    private static boolean init = false;

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
    public static void testSetup() {
        lifeCycle(LifeCycle.CONST);
        lifeCycle(LifeCycle.INIT);
        lifeCycle(LifeCycle.LOAD);
        lifeCycle(LifeCycle.ENABLE);
    }

    /**
     * 用于测试的快速注销方法
     * 会触发 DISABLE 生命周期
     */
    public static void testCancel() {
        lifeCycle(LifeCycle.DISABLE);
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
        if (platform != null) {
            TabooLibCommon.platform = platform;
        }
        switch (lifeCycle) {
            case CONST:
                try {
                    RuntimeEnv.ENV.setup();
                } catch (NoClassDefFoundError ignored) {
                }
                if (isKotlinEnvironment()) {
                    init = true;
                    PlatformFactory.INSTANCE.init();
                    RuntimeInjector.injectAll(LifeCycle.CONST);
                }
                break;
            case INIT:
                if (init) {
                    RuntimeInjector.injectAll(LifeCycle.INIT);
                }
                break;
            case LOAD:
                if (!init) {
                    if (isKotlinEnvironment()) {
                        init = true;
                        PlatformFactory.INSTANCE.init();
                        RuntimeInjector.injectAll(LifeCycle.CONST);
                        RuntimeInjector.injectAll(LifeCycle.INIT);
                    } else {
                        stopped = true;
                        throw new RuntimeException("Runtime environment setup failed, please feedback!");
                    }
                }
                RuntimeInjector.injectAll(LifeCycle.LOAD);
                break;
            case ENABLE:
                RuntimeInjector.injectAll(LifeCycle.ENABLE);
                break;
            case ACTIVE:
                RuntimeInjector.injectAll(LifeCycle.ACTIVE);
                break;
            case DISABLE:
                RuntimeInjector.injectAll(LifeCycle.DISABLE);
                PlatformFactory.INSTANCE.cancel();
                break;
        }
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
     * 停止 TabooLib 及插件加载流程
     */
    public static void setStopped(boolean value) {
        stopped = value;
    }

    public static boolean isSysoutCatcherFound() {
        return sysoutCatcherFound;
    }
}

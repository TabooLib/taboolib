package taboolib.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.env.ClassAppender;
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

    public static final RuntimeEnv ENV = new RuntimeEnv();

    /**
     * 保存最近一次初始化的运行环境
     */
    private static Platform platform = Platform.UNKNOWN;

    /**
     * 是否停止加载
     */
    private static boolean stopped = false;

    /**
     * 是否跳过 Kotlin 环境
     */
    private static boolean kotlinSkipped = false;

    private static boolean sysoutCatcherFound = false;

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
     */
    public static void testSetup() {
        lifeCycle(LifeCycle.CONST);
        lifeCycle(LifeCycle.INIT);
        lifeCycle(LifeCycle.LOAD);
        lifeCycle(LifeCycle.ENABLE);
    }

    /**
     * 用于测试的快速注销方法
     */
    public static void testCancel() {
        lifeCycle(LifeCycle.DISABLE);
    }

    public static void lifeCycle(LifeCycle lifeCycle) {
        lifeCycle(lifeCycle, null);
    }

    /**
     * 生命周期
     * 依赖于 Minecraft 服务端生命周期的启动或卸载方法
     *
     * @param lifeCycle 生命周期
     */
    public static void lifeCycle(LifeCycle lifeCycle, @Nullable Platform platform) {
        if (System.currentTimeMillis() > 1629123680000L) {
            throw new RuntimeException("The trial period of the plugin is over, please update!");
        }
        if (stopped) {
            return;
        }
        if (platform != null) {
            TabooLibCommon.platform = platform;
        }
        switch (lifeCycle) {
            case CONST:
                ENV.setup();
                PlatformFactory.INSTANCE.init();
                RuntimeInjector.INSTANCE.injectAll(LifeCycle.CONST);
                break;
            case INIT:
                RuntimeInjector.INSTANCE.injectAll(LifeCycle.INIT);
                break;
            case LOAD:
                RuntimeInjector.INSTANCE.injectAll(LifeCycle.LOAD);
                break;
            case ENABLE:
                RuntimeInjector.INSTANCE.injectAll(LifeCycle.ENABLE);
                break;
            case ACTIVE:
                RuntimeInjector.INSTANCE.injectAll(LifeCycle.ACTIVE);
                break;
            case DISABLE:
                RuntimeInjector.INSTANCE.injectAll(LifeCycle.DISABLE);
                PlatformFactory.INSTANCE.cancel();
                break;
        }
    }

    public static boolean isKotlinEnvironment() {
        return ClassAppender.isExists("kotlin.KotlinVersion");
    }

    @NotNull
    public static Platform getRunningPlatform() {
        return platform;
    }

    public static boolean isStopped() {
        return stopped;
    }

    public static void setStopped(boolean value) {
        stopped = value;
    }

    public static boolean isKotlinSkipped() {
        return kotlinSkipped;
    }

    public static void setKotlinSkipped(boolean value) {
        kotlinSkipped = value;
    }

    public static boolean isSysoutCatcherFound() {
        return sysoutCatcherFound;
    }
}

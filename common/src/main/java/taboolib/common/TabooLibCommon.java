package taboolib.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.env.ClassAppender;
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
public class TabooLibCommon {

    public static final RuntimeEnv ENV = new RuntimeEnv();

    /**
     * 保存最近一次初始化的运行环境
     */
    private static Platform platform = Platform.UNKNOWN;

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
        if (platform != null) {
            TabooLibCommon.platform = platform;
        }
        switch (lifeCycle) {
            case CONST:
                ENV.setup();
                PlatformFactory.INSTANCE.init();
                RuntimeInjector.INSTANCE.lifeCycle(LifeCycle.CONST);
                break;
            case INIT:
                RuntimeInjector.INSTANCE.lifeCycle(LifeCycle.INIT);
                break;
            case LOAD:
                RuntimeInjector.INSTANCE.lifeCycle(LifeCycle.LOAD);
                break;
            case ENABLE:
                RuntimeInjector.INSTANCE.lifeCycle(LifeCycle.ENABLE);
                break;
            case ACTIVE:
                RuntimeInjector.INSTANCE.lifeCycle(LifeCycle.ACTIVE);
                break;
            case DISABLE:
                RuntimeInjector.INSTANCE.lifeCycle(LifeCycle.DISABLE);
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
}

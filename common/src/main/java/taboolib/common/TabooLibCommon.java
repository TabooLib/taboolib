package taboolib.common;

import taboolib.common.env.ClassAppender;
import taboolib.common.env.RuntimeDependency;
import taboolib.common.env.RuntimeEnv;
import taboolib.common.inject.RuntimeInjector;
import taboolib.common.platform.PlatformFactory;

import java.io.IOException;

/**
 * TabooLib
 * taboolib.common.TabooLibCommon
 *
 * @author sky
 * @since 2021/6/15 2:45 下午
 */
@RuntimeDependency(value = "org.jetbrains.kotlin:kotlin-stdlib:1.5.20-RC", test = "kotlin.KotlinVersion")
@RuntimeDependency(value = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.20-RC", test = "kotlin.jdk7.AutoCloseableKt")
@RuntimeDependency(value = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.20-RC", test = "kotlin.collections.jdk8.CollectionsJDK8Kt")
public class TabooLibCommon {

    public static final RuntimeEnv ENV = new RuntimeEnv();

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

    /**
     * 依赖于 Minecraft 服务端生命周期的启动或卸载方法
     */
    public static void lifeCycle(LifeCycle lifeCycle) {
        switch (lifeCycle) {
            case CONST:
                ENV.inject(TabooLibCommon.class);
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
                PlatformFactory.INSTANCE.cancel();
                break;
        }
    }

    public static boolean isKotlinEnvironment() {
        return ClassAppender.isExists("kotlin.KotlinVersion");
    }
}

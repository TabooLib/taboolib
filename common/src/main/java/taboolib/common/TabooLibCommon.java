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

    public static void init() {
        ENV.inject(TabooLibCommon.class, null);
        PlatformFactory.INSTANCE.init();
        RuntimeInjector.INSTANCE.register(ENV);
        RuntimeInjector.INSTANCE.init();
    }

    public static void cancel() {
        PlatformFactory.INSTANCE.cancel();
    }

    public static boolean isKotlinEnvironment() {
        return ClassAppender.isExists("kotlin.KotlinVersion");
    }
}

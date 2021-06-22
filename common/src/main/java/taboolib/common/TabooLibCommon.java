package taboolib.common;

import taboolib.common.env.ClassAppender;
import taboolib.common.env.RuntimeDependency;
import taboolib.common.env.RuntimeEnv;
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
@RuntimeDependency(value = "org.jetbrains.kotlin:kotlin-reflect:1.5.20-RC", test = "kotlin.reflect.jvm.KClassesJvm")
public class TabooLibCommon {

    public static void init() {
        try {
            // 加载 Kotlin 运行库
            RuntimeEnv.inject(TabooLibCommon.class);
            // 初始化跨平台接口
            PlatformFactory.INSTANCE.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isKotlinEnvironment() {
        return ClassAppender.isExists("kotlin.KotlinVersion");
    }
}

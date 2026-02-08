package taboolib.common.env;

import org.jetbrains.annotations.NotNull;
import org.tabooproject.reflex.ReflexClass;
import taboolib.common.PrimitiveIO;
import taboolib.common.PrimitiveSettings;
import taboolib.common.TabooLib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static taboolib.common.PrimitiveSettings.KOTLIN_COROUTINES_VERSION;
import static taboolib.common.PrimitiveSettings.KOTLIN_VERSION;

/**
 * TabooLib
 * taboolib.common.env.RuntimeEnv
 *
 * @author sky
 * @since 2021/6/15 6:23 下午
 */
@SuppressWarnings("CallToPrintStackTrace")
public class RuntimeEnv {

    public static final String KOTLIN_ID = "!kotlin".substring(1);
    public static final String KOTLIN_COROUTINES_ID = "!kotlinx.coroutines".substring(1);

    public static final RuntimeEnv ENV = new RuntimeEnv();
    public static final RuntimeEnvAssets ENV_ASSETS = new RuntimeEnvAssets();
    public static final RuntimeEnvDependency ENV_DEPENDENCY = new RuntimeEnvDependency();

    /**
     * 初始化运行时环境，由 extra.properties 调用
     * 用于初始化 Kotlin 环境
     */
    static void init() {
        PrimitiveIO.debug("RuntimeEnv 加载完成，用时 {0} 毫秒。", TabooLib.execution(() -> {
            List<JarRelocation> rel = new ArrayList<>();
            boolean loadKotlin = !KOTLIN_VERSION.equals("null");
            boolean loadKotlinCoroutines = !KOTLIN_COROUTINES_VERSION.equals("null");
            // 非隔离模式
            if (!PrimitiveSettings.IS_ISOLATED_MODE) {
                // 启用重定向
                rel.add(new JarRelocation(KOTLIN_ID + ".", PrimitiveSettings.getRelocatedKotlinVersion() + "."));
                rel.add(new JarRelocation(KOTLIN_COROUTINES_ID + ".", PrimitiveSettings.getRelocatedKotlinCoroutinesVersion() + "."));
                // 启用环境检查
                // 在隔离模式下不会检查 Kotlin 环境，只要定义版本必定加载
                if (TabooLib.isKotlinEnvironment()) loadKotlin = false;
                if (TabooLib.isKotlinCoroutinesEnvironment()) loadKotlinCoroutines = false;
            }
            // 加载 Kotlin 环境
            if (loadKotlin) {
                try {
                    ENV_DEPENDENCY.loadDependency("org.jetbrains.kotlin:kotlin-stdlib:" + KOTLIN_VERSION, false, rel);
                    ENV_DEPENDENCY.loadDependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8:" + KOTLIN_VERSION, false, rel);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            // 加载 Kotlin Coroutines 环境
            if (loadKotlinCoroutines) {
                try {
                    ENV_DEPENDENCY.loadDependency("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:" + KOTLIN_COROUTINES_VERSION, false, rel);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            // 加载本地文件定义的依赖
            try {
                URL url = RuntimeEnvDependency.class.getClassLoader().getResource("META-INF/taboolib/dependency.json");
                ENV_DEPENDENCY.loadFromLocalFile(url);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public int inject(@NotNull ReflexClass clazz) throws Throwable {
        int total = 0;
        total += ENV_ASSETS.loadAssets(clazz);
        total += ENV_DEPENDENCY.loadDependency(clazz);
        return total;
    }
}

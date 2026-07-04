package taboolib.common.env;

import org.jetbrains.annotations.NotNull;
import org.tabooproject.reflex.ReflexClass;
import taboolib.common.ClassAppender;
import taboolib.common.PrimitiveIO;
import taboolib.common.PrimitiveSettings;
import taboolib.common.TabooLib;

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
                // 加载 Kotlin Coroutines 环境
                if (loadKotlinCoroutines) {
                    try {
                        ENV_DEPENDENCY.loadDependency("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:" + KOTLIN_COROUTINES_VERSION, false, rel);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            // 加载本地文件定义的依赖
            try {
                ENV_DEPENDENCY.loadFromLocalFile(RuntimeEnvDependency.class.getClassLoader().getResource("META-INF/taboolib/dependency.json"));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            // 在【启动主线程】预热协程运行时，规避其首次初始化落在 AsyncPlayerPreLogin 等敌对线程上导致的
            // 非确定性崩溃（多 TabooLib 插件共存时偶发，详见 warmupKotlinCoroutines）。
            if (!KOTLIN_COROUTINES_VERSION.equals("null")) {
                warmupKotlinCoroutines();
            }
        }));
    }

    /**
     * 在【启动主线程】预热（强制初始化）协程运行时中那些「{@code <clinit>} 经 {@link java.util.ServiceLoader}
     * 装配、对首次初始化所在线程 / 类加载器上下文敏感」的类。
     *
     * <h3>背景：为什么需要它</h3>
     * 非隔离模式（默认）下，TabooLib 把 kotlinx-coroutines 重定位成版本键控的共享包
     * （{@link PrimitiveSettings#getRelocatedKotlinCoroutinesVersion()}，如 {@code kotlin2120x.coroutines1101}）
     * 加载进各插件的 PluginClassLoader；由于包名跨插件相同，Paper 的插件类加载器组会让它在多个 TabooLib 插件间
     * 共享 / 首加载者定义。而 {@code Dispatchers} / {@code CoroutineExceptionHandler} 的首次初始化走 ServiceLoader，
     * 一旦它**首次**被触发的线程是 Bukkit 的 {@code AsyncPlayerPreLogin}（"User Authenticator"）这类敌对线程
     * （contextClassLoader 非插件 CL），初始化会非确定性失败（{@code NoClassDefFoundError}）并**永久毒化该共享类**，
     * 使此后所有 TabooLib 插件的协程全部不可用。该问题仅在多个 TabooLib 插件共存时偶发，且极难排查。
     *
     * <h3>修复原理</h3>
     * 在插件加载阶段（{@link #init()}，运行于启动主线程）就用 {@link Class#forName(String, boolean, ClassLoader)}
     * 强制这些类完成首次初始化。JVM 保证类只初始化一次，之后任意线程再触碰都直接复用，不会再触发脆弱的首次初始化。
     * 预热为尽力而为：任何失败都被吞掉，绝不影响插件正常加载。
     */
    private static void warmupKotlinCoroutines() {
        try {
            ClassLoader loader = ClassAppender.getClassLoader();
            // 协程可能以「重定位包」(非隔离模式由 TabooLib 加载) 或「原始包」(隔离模式 / 服务端自带 / 其它来源) 存在；
            // 两种包名都尝试，命中哪个就预热哪个，未命中的经 try/catch 无害空转。
            int warmed = 0;
            warmed += warmupCoroutinePackage(loader, PrimitiveSettings.getRelocatedKotlinCoroutinesVersion());
            warmed += warmupCoroutinePackage(loader, KOTLIN_COROUTINES_ID);
            if (warmed > 0) {
                PrimitiveIO.debug("协程运行时已在启动线程 [{0}] 预热 {1} 个类。", Thread.currentThread().getName(), warmed);
            }
        } catch (Throwable ignored) {
            // 预热为尽力而为，绝不因其失败而中断插件加载。
        }
    }

    private static int warmupCoroutinePackage(ClassLoader loader, String pkg) {
        // 这些类的 <clinit> 含 ServiceLoader / 对线程敏感，是历史登录崩溃的炸点；强制其在当前(启动主)线程初始化。
        // 类名随协程版本可能有差异，逐个 try/catch（不存在则无害跳过）；其中 Dispatchers / CoroutineExceptionHandler 名称稳定。
        String[] classes = {
                pkg + ".Dispatchers",
                pkg + ".CoroutineExceptionHandler",
                pkg + ".internal.MainDispatcherLoader",
                pkg + ".CoroutineExceptionHandlerImplKt",
                pkg + ".internal.CoroutineExceptionHandlerImplKt",
        };
        int warmed = 0;
        for (String name : classes) {
            try {
                Class.forName(name, true, loader);
                warmed++;
            } catch (Throwable ignored) {
                // 该类不存在（版本差异）或该包未加载，忽略。
            }
        }
        return warmed;
    }

    public int inject(@NotNull ReflexClass clazz) throws Throwable {
        int total = 0;
        total += ENV_ASSETS.loadAssets(clazz);
        total += ENV_DEPENDENCY.loadDependency(clazz);
        return total;
    }
}

package taboolib.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import taboolib.common.env.RuntimeDependency;
import taboolib.common.env.RuntimeEnv;
import taboolib.common.inject.VisitorHandler;
import taboolib.common.platform.Platform;
import taboolib.common.platform.PlatformFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * TabooLib
 * taboolib.common.TabooLibCommon
 *
 * @author sky
 * @since 2021/6/15 2:45 下午
 */
@RuntimeDependency(value = "!com.google.code.gson:gson:2.8.7", test = "!com.google.gson.JsonElement")
public class TabooLibCommon {

    /** 当前插件文件名 **/
    private static String runningFileName = "TabooLib";

    /** 当前运行环境 **/
    private static Platform runningPlatform = Platform.APPLICATION;

    /** 当前生命周期 **/
    private static LifeCycle currentLifeCycle = LifeCycle.CONST;

    /** 是否停止加载 **/
    private static boolean isStopped = false;

    /** Kotlin 环境是否就绪 **/
    private static boolean isKotlinLoaded = false;

    /** 是否被 Paper 核心拦截控制台打印 **/
    private static boolean isSysoutCatcherFound = false;

    /** 推迟任务 **/
    private static final Map<LifeCycle, List<Runnable>> postponeExecutor = new ConcurrentHashMap<>();

    static {
        // 获取插件文件
        try {
            runningFileName = new File(TabooLibCommon.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getName();
        } catch (Throwable ignored) {
        }
        // 检查 Paper 核心控制台拦截工具
        try {
            Class.forName("io.papermc.paper.logging.SysoutCatcher");
            isSysoutCatcherFound = true;
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
     * 推迟任务到指定生命周期下执行
     */
    public static void postpone(LifeCycle lifeCycle, Runnable runnable) {
        if (lifeCycle.ordinal() >= TabooLibCommon.currentLifeCycle.ordinal()) {
            runnable.run();
        } else {
            postponeExecutor.computeIfAbsent(lifeCycle, k -> new ArrayList<>()).add(runnable);
        }
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
        if (isStopped) {
            return;
        }
        // 记录运行环境
        if (platform != null) {
            runningPlatform = platform;
        }
        // 记录生命周期
        currentLifeCycle = lifeCycle;
        // 运行推迟任务
        postponeExecutor.forEach((cycle, list) -> {
            if (cycle == lifeCycle) {
                list.forEach((runnable) -> {
                    try {
                        runnable.run();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                });
                postponeExecutor.remove(cycle);
            }
        });
        // 开发者模式下打印生命周期
        if (isDevelopmentMode()) {
            TabooLibCommon.print("LifeCycle: " + lifeCycle);
        }
        // 检查生命周期
        switch (lifeCycle) {
            case CONST: {
                // 加载运行环境
                // 初始化 RuntimeEnv 模块
                try {
                    if (TabooLibCommon.isDevelopmentMode()) {
                        TabooLibCommon.print("RuntimeEnv setup...");
                    }
                    RuntimeEnv.ENV.setup();
                } catch (NoClassDefFoundError ignored) {
                    if (TabooLibCommon.isDevelopmentMode()) {
                        TabooLibCommon.print("RuntimeEnv not found.");
                    }
                }
                // Kotlin 环境已就绪
                if (TabooLibCommon.isKotlinEnvironment()) {
                    isKotlinLoaded = true;
                    PlatformFactory.INSTANCE.init();
                    VisitorHandler.injectAll(LifeCycle.CONST);
                }
                break;
            }
            case INIT: {
                if (isKotlinLoaded) {
                    // 依赖注入
                    VisitorHandler.injectAll(LifeCycle.INIT);
                }
                break;
            }
            case LOAD: {
                // 若在 CONST 生命周期中未加载 Kotlin 环境，则尝试重新检测并再次启动
                if (!isKotlinLoaded) {
                    if (isKotlinEnvironment()) {
                        isKotlinLoaded = true;
                        PlatformFactory.INSTANCE.init();
                        VisitorHandler.injectAll(LifeCycle.CONST);
                        VisitorHandler.injectAll(LifeCycle.INIT);
                    } else {
                        isStopped = true;
                        String testClass = "kotlin.Lazy";
                        throw new RuntimeException("Runtime environment setup failed, please feedback! (test: " + testClass + ", " + "classloader: " + TabooLibCommon.class.getClassLoader() + ")");
                    }
                }
                VisitorHandler.injectAll(LifeCycle.LOAD);
                break;
            }
            case ENABLE: {
                VisitorHandler.injectAll(LifeCycle.ENABLE);
                break;
            }
            case ACTIVE: {
                VisitorHandler.injectAll(LifeCycle.ACTIVE);
                break;
            }
            case DISABLE: {
                VisitorHandler.injectAll(LifeCycle.DISABLE);
                PlatformFactory.INSTANCE.cancel();
                break;
            }
        }
    }

    /**
     * 检查当前 Kotlin 环境是否有效
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
     * 检查当前是否处于开发者模式
     */
    public static boolean isDevelopmentMode() {
        return new File("dev").exists();
    }

    /**
     * 获取当前运行平台
     */
    @NotNull
    public static Platform getRunningPlatform() {
        return runningPlatform;
    }

    /**
     * 获取当前生命周期
     */
    @NotNull
    public static LifeCycle getLifeCycle() {
        return currentLifeCycle;
    }

    /**
     * 是否被 Paper 核心拦截控制台打印
     */
    public static boolean isSysoutCatcherFound() {
        return isSysoutCatcherFound;
    }

    /**
     * 是否停止 TabooLib 及插件加载流程
     */
    public static boolean isStopped() {
        return isStopped;
    }

    /**
     * 停止 TabooLib 及插件加载流程
     */
    public static void setStopped(boolean value) {
        isStopped = value;
    }

    /**
     * 控制台输出
     */
    public static void print(Object message) {
        if (TabooLibCommon.isSysoutCatcherFound()) {
            Logger.getLogger(runningFileName).info(Objects.toString(message));
        } else {
            System.out.println(message);
        }
    }
}

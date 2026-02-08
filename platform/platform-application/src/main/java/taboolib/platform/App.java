package taboolib.platform;

import taboolib.common.LifeCycle;
import taboolib.common.PrimitiveIO;
import taboolib.common.TabooLib;
import taboolib.common.classloader.IsolatedClassLoader;

import java.io.File;

/**
 * TabooLib
 * taboolib.platform.App
 *
 * @author 坏黑
 * @since 2024/1/26 21:43
 */
public class App {

    static {
        // 如果是 Application 启动，则跳过重定向
        env().skipSelfRelocate(true).skipKotlinRelocate(true);
        // 如果 App 和 TabooLib 不在一个文件里
        if (!App.class.getProtectionDomain().getCodeSource().getLocation().sameFile(TabooLib.class.getProtectionDomain().getCodeSource().getLocation())) {
            env().scan(App.class.getName());
        }
    }

    /**
     * 环境变量设置
     */
    public static AppEnv env() {
        return new AppEnv();
    }

    /**
     * 启动
     */
    public static void init() {
        // 初始化环境参数
        setupEnv();
        // 启动 TabooLib
        PrimitiveIO.debug("App 初始化完成，用时 {0} 毫秒。", TabooLib.execution(() -> {
            // 初始化 IsolatedClassLoader
            IsolatedClassLoader.init(App.class);
            // 生命周期任务
            TabooLib.lifeCycle(LifeCycle.CONST);
            TabooLib.lifeCycle(LifeCycle.INIT);
            TabooLib.lifeCycle(LifeCycle.LOAD);
            TabooLib.lifeCycle(LifeCycle.ENABLE);
        }));
    }

    /**
     * 结束
     */
    public static void shutdown() {
        TabooLib.lifeCycle(LifeCycle.DISABLE);
    }

    /**
     * 初始化基本信息
     */
    static void setupEnv() {
        String command = System.getProperty("sun.java.command", "").replace(File.separator, ".");
        try {
            // 通过 IDEA 直接运行
            Class.forName(command);
            System.setProperty("taboolib.main", command);
            String group = matchGroup(command);
            System.setProperty("taboolib.group", group);
            PrimitiveIO.println("Running in IDE mode. (main: {0}, group: {1})", command, group);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 尝试从主类中获取 group 信息
     */
    static String matchGroup(String main) {
        String[] args = main.split("\\.");
        switch (args.length) {
            case 1:
                PrimitiveIO.error("Unable to match group from main class. (main: " + main + ")");
                PrimitiveIO.error("Please use App.env().group(\"xxx\") to set the group.");
                return args[0];
            case 2:
                return args[0];
            default:
                return args[0] + "." + args[1];
        }
    }
}

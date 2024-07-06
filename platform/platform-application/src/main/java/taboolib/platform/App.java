package taboolib.platform;

import taboolib.common.LifeCycle;
import taboolib.common.PrimitiveIO;
import taboolib.common.TabooLib;
import taboolib.common.classloader.IsolatedClassLoader;

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
        long time = System.currentTimeMillis();
        // 初始化 IsolatedClassLoader
        IsolatedClassLoader.init(App.class);
        // 生命周期任务
        TabooLib.lifeCycle(LifeCycle.CONST);
        TabooLib.lifeCycle(LifeCycle.INIT);
        TabooLib.lifeCycle(LifeCycle.LOAD);
        TabooLib.lifeCycle(LifeCycle.ENABLE);
        // 调试模式显示加载耗时
        PrimitiveIO.dev("[TabooLib] \"%s\" Initialization completed. (%sms)", PrimitiveIO.getRunningFileName(), System.currentTimeMillis() - time);
    }

    /**
     * 结束
     */
    public static void shutdown() {
        TabooLib.lifeCycle(LifeCycle.DISABLE);
    }
}

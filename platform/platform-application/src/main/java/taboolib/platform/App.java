package taboolib.platform;

import taboolib.common.LifeCycle;
import taboolib.common.PrimitiveIO;
import taboolib.common.PrimitiveSettings;
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
        if (PrimitiveSettings.IS_DEV_MODE) {
            PrimitiveIO.println("[TabooLib] \"%s\" Initialization completed. (%sms)", PrimitiveIO.getRunningFileName(), System.currentTimeMillis() - time);
        }
    }

    /**
     * 结束
     */
    public static void shutdown() {
        TabooLib.lifeCycle(LifeCycle.DISABLE);
    }
}

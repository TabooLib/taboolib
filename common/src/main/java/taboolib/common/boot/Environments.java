package taboolib.common.boot;

import taboolib.common.TabooLib;
import taboolib.common.env.RuntimeEnv;

/**
 * TabooLib
 * taboolib.common.boot.Environments
 *
 * @author 坏黑
 * @since 2022/1/25 2:06 AM
 */
public class Environments {

    Environments() {
    }

    public static boolean isKotlin() {
        try {
            Class.forName("kotlin.Lazy", false, TabooLib.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * 是否存在 Paper 日志拦截
     * 它将会阻止插件在未加载之前输出日志
     */
    public static boolean isPaperSysoutCatcher() {
        try {
            Class.forName("io.papermc.paper.logging.SysoutCatcher", false, TabooLib.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}

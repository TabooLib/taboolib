package taboolib.common.platform;

import org.jetbrains.annotations.Nullable;
import taboolib.common.ClassAppender;

import java.io.File;

/**
 * TabooLib
 * taboolib.plugin.Plugin
 *
 * @author sky
 * @since 2021/6/15 6:19 下午
 */
public abstract class Plugin {

    /**
     * 当加载插件时调用
     */
    public void onLoad() {
    }

    /**
     * 当启用插件时调用
     */
    public void onEnable() {
    }

    /**
     * 当服务器启动完成时调用
     */
    public void onActive() {
    }

    /**
     * 当卸载插件时调用
     */
    public void onDisable() {
    }

    /**
     * 重定向插件文件（用于改变 TabooLib 逻辑）
     */
    @Nullable
    public File nativeJarFile() {
        return null;
    }

    /**
     * 重定向插件目录（用于改变 TabooLib 逻辑）
     */
    @Nullable
    public File nativeDataFolder() {
        return null;
    }

    /**
     * 获取实现
     */
    public static Plugin findImpl() {
        try {
            Class<?> scanner = Class.forName("taboolib.common.io.ProjectScannerKt", true, ClassAppender.getClassLoader());
            Object impl = scanner.getDeclaredMethod("findPluginImpl").invoke(null);
            return impl != null ? (Plugin) impl : null;
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to find plugin implementation.", ex);
        }
    }
}
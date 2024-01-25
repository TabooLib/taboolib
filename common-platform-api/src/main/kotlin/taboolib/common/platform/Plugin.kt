package taboolib.common.platform

import java.io.File

/**
 * TabooLib
 * taboolib.plugin.Plugin
 *
 * @author sky
 * @since 2021/6/15 6:19 下午
 */
abstract class Plugin {

    /**
     * 当加载插件时调用
     */
    open fun onLoad() = Unit

    /**
     * 当启用插件时调用
     */
    open fun onEnable() = Unit

    /**
     * 当服务器启动完成时调用
     */
    open fun onActive() = Unit

    /**
     * 当卸载插件时调用
     */
    open fun onDisable() = Unit

    /**
     * 重定向插件文件（用于改变 TabooLib 逻辑）
     */
    open fun nativeJarFile(): File? {
        return null
    }

    /**
     * 重定向插件目录（用于改变 TabooLib 逻辑）
     */
    open fun nativeDataFolder(): File? {
        return null
    }
}
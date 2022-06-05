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

    open fun onLoad() {
    }

    open fun onEnable() {
    }

    open fun onActive() {
    }

    open fun onDisable() {
    }

    open fun nativeJarFile(): File? {
        return null
    }

    open fun nativeDataFolder(): File? {
        return null
    }
}
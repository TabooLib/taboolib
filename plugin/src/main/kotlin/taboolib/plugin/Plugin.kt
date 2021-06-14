package taboolib.plugin

/**
 * TabooLib
 * taboolib.plugin.Plugin
 *
 * @author sky
 * @since 2021/6/14 10:53 下午
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
}
package taboolib.common.platform

import taboolib.common.Isolated

/**
 * TabooLib
 * taboolib.plugin.Plugin
 *
 * @author sky
 * @since 2021/6/15 6:19 下午
 */
@Isolated
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
package taboolib.common.platform

/**
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
}
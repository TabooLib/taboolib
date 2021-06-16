package taboolib.common.platform

/**
 * TabooLib
 * taboolib.common.platform.ProxyEvent
 *
 * @author sky
 * @since 2021/6/17 1:00 上午
 */
abstract class ProxyEvent {

    open val allowCancelled: Boolean
        get() = true

    open val allowAsynchronous: Boolean
        get() = true

    var isCancelled = false

    open fun call(): Boolean {
        callEvent(this)
        return !isCancelled
    }

    open fun postCall() {
    }
}
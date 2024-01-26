package taboolib.common.event

/**
 * TabooLib
 * taboolib.common.event.InternalEvent
 *
 * @author 坏黑
 * @since 2024/1/26 15:30
 */
abstract class InternalEvent {

    fun call() = InternalEventBus.call(this)
}

abstract class CancelableInternalEvent : InternalEvent() {

    var isCancelled = false

    fun callIf(): Boolean {
        call()
        return !isCancelled
    }
}
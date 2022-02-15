package taboolib.common.platform.event

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformEvent

/**
 * @author sky
 * @since 2021/6/17 1:00 上午
 */
@Suppress("UnnecessaryAbstractClass", "EmptyFunctionBlock")
abstract class ProxyEvent {

    open val allowCancelled = true

    var isCancelled = false

    open fun call(): Boolean {
        PlatformFactory.getPlatformService<PlatformEvent>().callEvent(this)
        return !isCancelled
    }

    fun postCall() {}
}

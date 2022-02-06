package taboolib.platform.type

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import taboolib.common.platform.event.ProxyEvent
import taboolib.platform.VelocityPlugin

open class VelocityProxyEvent(val proxyEvent: ProxyEvent? = null) : ResultedEvent<GenericResult> {

    private var isCancelled = false

    open val allowCancelled: Boolean
        get() = true

    override fun getResult(): GenericResult {
        return if (proxyEvent?.isCancelled ?: isCancelled) GenericResult.denied() else GenericResult.allowed()
    }

    override fun setResult(result: GenericResult) {
        if (proxyEvent != null) {
            if (proxyEvent.allowCancelled) {
                proxyEvent.isCancelled = !result.isAllowed
            } else {
                error("Unsupported")
            }
        } else if (allowCancelled) {
            isCancelled = !result.isAllowed
        } else {
            error("Unsupported")
        }
    }

    fun call(): Boolean {
        VelocityPlugin.getInstance().server.eventManager.fire(this)
        return !isCancelled
    }
}
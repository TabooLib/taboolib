package taboolib.platform.type

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.ResultedEvent.GenericResult
import taboolib.platform.VelocityPlugin

open class VelocityProxyEvent : ResultedEvent<GenericResult> {

    private var isCancelled = false

    open val allowCancelled: Boolean
        get() = true

    override fun getResult(): GenericResult {
        return if (isCancelled) GenericResult.denied() else GenericResult.allowed()
    }

    override fun setResult(result: GenericResult) {
        if (allowCancelled) {
            isCancelled = !result.isAllowed
        } else {
            error("Event cannot be cancelled.")
        }
    }

    fun call(): Boolean {
        VelocityPlugin.getInstance().server.eventManager.fire(this)
        return !isCancelled
    }
}
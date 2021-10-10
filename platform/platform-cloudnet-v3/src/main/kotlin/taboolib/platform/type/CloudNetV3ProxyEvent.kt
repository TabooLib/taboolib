package taboolib.platform.type

import de.dytanic.cloudnet.CloudNet
import de.dytanic.cloudnet.driver.event.Event
import de.dytanic.cloudnet.driver.event.ICancelable
import taboolib.common.platform.event.ProxyEvent

open class CloudNetV3ProxyEvent(val proxyEvent: ProxyEvent? = null) : Event(), ICancelable {

    private var isCancelled = false

    open val allowCancelled: Boolean
        get() = true

    override fun isCancelled(): Boolean {
        return proxyEvent?.isCancelled ?: isCancelled
    }

    override fun setCancelled(value: Boolean) {
        if (proxyEvent != null) {
            if (proxyEvent.allowCancelled) {
                proxyEvent.isCancelled = value
            } else {
                error("Unsupported")
            }
        } else if (allowCancelled) {
            isCancelled = value
        } else {
            error("unsupported")
        }
    }

    fun call(): Boolean {
        CloudNet.getInstance().eventManager.callEvent(this)
        return !isCancelled
    }
}
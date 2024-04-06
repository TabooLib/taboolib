package taboolib.platform.type


import net.afyer.afybroker.server.Broker
import net.afyer.afybroker.server.plugin.Cancellable
import net.afyer.afybroker.server.plugin.Event

open class AfyBrokerProxyEvent : Event(), Cancellable {

    private var isCancelled = false

    open val allowCancelled: Boolean
        get() = true

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(value: Boolean) {
        if (allowCancelled) {
            isCancelled = value
        } else {
            error("Event cannot be cancelled.")
        }
    }

    override fun call(): Boolean {
        Broker.getPluginManager().callEvent(this)
        return !isCancelled
    }
}
package taboolib.platform

import org.spongepowered.api.Sponge
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent
import taboolib.platform.type.Sponge7ProxyEvent

/**
 * TabooLib
 * taboolib.platform.SpongeAdapter
 *
 * @author tr
 * @since 2021/6/21 17:02
 */
@Awake
@PlatformSide([Platform.SPONGE_API_7])
class Sponge7Event : PlatformEvent {

    override fun callEvent(proxyEvent: ProxyEvent) {
        val event = Sponge7ProxyEvent(proxyEvent)
        Sponge.getEventManager().post(event)
        event.proxyEvent?.postCall()
    }
}
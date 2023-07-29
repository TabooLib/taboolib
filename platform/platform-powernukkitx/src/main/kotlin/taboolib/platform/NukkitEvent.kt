package taboolib.platform

import cn.nukkit.Server
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent
import taboolib.platform.type.NukkitProxyEvent

/**
 * TabooLib
 * starslib.platform.NukkitAdapter
 *
 * @author CziSKY
 * @since 2021/6/20 0:46
 */
@Awake
@PlatformSide([Platform.NUKKIT])
class NukkitEvent : PlatformEvent {

    override fun callEvent(proxyEvent: ProxyEvent) {
        Server.getInstance().pluginManager.callEvent(NukkitProxyEvent(proxyEvent).also {
            it.proxyEvent?.postCall()
        })
    }
}
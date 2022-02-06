package taboolib.platform

import cn.nukkit.Server
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent
import taboolib.internal.Internal
import taboolib.platform.type.NukkitProxyEvent

/**
 * TabooLib
 * taboolib.platform.NukkitAdapter
 *
 * @author CziSKY
 * @since 2021/6/20 0:46
 */
@Internal
@Awake
@PlatformSide([Platform.NUKKIT])
class NukkitEvent : PlatformEvent {

    override fun callEvent(proxyEvent: ProxyEvent) {
        Server.getInstance().pluginManager.callEvent(NukkitProxyEvent(proxyEvent))
        proxyEvent.postCall()
    }
}
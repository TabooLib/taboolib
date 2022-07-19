package taboolib.platform

import de.dytanic.cloudnet.CloudNet
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.service.PlatformEvent
import taboolib.platform.type.CloudNetV3ProxyEvent

/**
 * TabooLib
 * taboolib.platform.BungeeAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@PlatformSide([Platform.CLOUDNET_V3])
class CloudNetV3Event : PlatformEvent {

    val plugin by unsafeLazy { CloudNetV3Plugin.getInstance() }

    override fun callEvent(proxyEvent: ProxyEvent) {
        val event = CloudNetV3ProxyEvent(proxyEvent)
        CloudNet.getInstance().eventManager.callEvent(event)
        event.proxyEvent?.postCall()
    }
}
package taboolib.platform

import de.dytanic.cloudnet.CloudNet
import taboolib.common.OpenContainer
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformOpenContainer
import taboolib.platform.type.CloudNetV3OpenContainer

/**
 * TabooLib
 * taboolib.platform.BungeeAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@PlatformSide([Platform.CLOUDNET_V3])
class CloudNetV3OpenContainer : PlatformOpenContainer {

    val pluginContainer = HashMap<String, OpenContainer>()

    override fun getOpenContainers(): List<OpenContainer> {
        return CloudNet.getInstance().moduleProvider.modules.filter {
            it.module.javaClass.name.endsWith("platform.BungeePlugin") }.mapNotNull {
            pluginContainer.computeIfAbsent(it.moduleConfiguration.name) { _ -> CloudNetV3OpenContainer(it.module) }
        }
    }
}
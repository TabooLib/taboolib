package taboolib.platform

import taboolib.common.OpenContainer
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.service.PlatformOpenContainer
import taboolib.common.util.orNull
import taboolib.platform.type.VelocityContainer

/**
 * TabooLib
 * taboolib.platform.VelocityIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@PlatformSide(Platform.VELOCITY)
class VelocityOpenContainer : PlatformOpenContainer {

    val pluginContainer = HashMap<String, OpenContainer>()

    override fun getOpenContainers(): List<OpenContainer> {
        return VelocityPlugin.getInstance().server.pluginManager.plugins
            .filter { it.instance.orElse(null)?.javaClass?.name?.endsWith("platform.VelocityPlugin") == true && it.description.name.orNull() != pluginId }
            .mapNotNull {
                pluginContainer.computeIfAbsent(it.description.id) { _ -> VelocityContainer(it) }
            }
    }
}
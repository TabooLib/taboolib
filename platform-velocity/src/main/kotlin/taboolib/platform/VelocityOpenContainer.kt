package taboolib.platform

import taboolib.common.OpenContainer
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformOpenContainer
import taboolib.platform.type.VelocityOpenContainer

/**
 * TabooLib
 * taboolib.platform.VelocityIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@PlatformSide([Platform.VELOCITY])
class VelocityOpenContainer : PlatformOpenContainer {

    val pluginContainer = HashMap<String, OpenContainer>()

    override fun getOpenContainers(): List<OpenContainer> {
        val plugins = VelocityPlugin.getInstance().server.pluginManager.plugins
        return plugins.filter { it.instance.orElse(null)?.javaClass?.name?.endsWith("platform.VelocityPlugin") == true }.mapNotNull {
            pluginContainer.computeIfAbsent(it.description.id) { _ -> VelocityOpenContainer(it) }
        }
    }
}
package taboolib.platform

import net.md_5.bungee.BungeeCord
import taboolib.common.Inject
import taboolib.common.OpenContainer
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.service.PlatformOpenContainer
import taboolib.platform.type.BungeeContainer

/**
 * TabooLib
 * taboolib.platform.BungeeAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@Inject
@PlatformSide(Platform.BUNGEE)
class BungeeOpenContainer : PlatformOpenContainer {

    val pluginContainer = HashMap<String, OpenContainer>()

    override fun getOpenContainers(): List<OpenContainer> {
        return BungeeCord.getInstance().pluginManager.plugins.filter { it.javaClass.name.endsWith("platform.BungeePlugin") && it.description.name != pluginId }.mapNotNull {
            pluginContainer.computeIfAbsent(it.description.name) { _ -> BungeeContainer(it) }
        }
    }
}
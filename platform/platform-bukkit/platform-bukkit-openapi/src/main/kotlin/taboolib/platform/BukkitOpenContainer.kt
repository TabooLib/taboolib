package taboolib.platform

import org.bukkit.Bukkit
import taboolib.internal.Internal
import taboolib.common.OpenContainer
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformOpenContainer
import taboolib.platform.type.BukkitOpenContainer

/**
 * TabooLib
 * taboolib.platform.BukkitIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Internal
@Awake
@PlatformSide([Platform.BUKKIT])
class BukkitOpenContainer : PlatformOpenContainer {

    val pluginContainer = HashMap<String, OpenContainer>()

    override fun getOpenContainers(): List<OpenContainer> {
        return Bukkit.getPluginManager().plugins.filter { it.javaClass.name.endsWith("platform.BukkitPlugin") }.mapNotNull {
            pluginContainer.computeIfAbsent(it.name) { _ -> BukkitOpenContainer(it) }
        }
    }
}
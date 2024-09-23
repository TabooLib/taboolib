package taboolib.platform

import org.bukkit.Bukkit
import taboolib.common.Inject
import taboolib.common.OpenContainer
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.service.PlatformOpenContainer
import taboolib.platform.type.BukkitContainer

/**
 * TabooLib
 * taboolib.platform.BukkitIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@Inject
@PlatformSide(Platform.BUKKIT)
class BukkitOpenContainer : PlatformOpenContainer {

    val pluginContainer = HashMap<String, OpenContainer>()

    override fun getOpenContainers(): List<OpenContainer> {
        return Bukkit.getPluginManager().plugins.filter { it.javaClass.name.endsWith("platform.BukkitPlugin") && it.name != pluginId }.mapNotNull {
            pluginContainer.getOrPut(it.name) { BukkitContainer(it) }
        }.filter {
            it.isValid
        }
    }
}
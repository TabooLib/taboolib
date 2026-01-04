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
import java.util.concurrent.ConcurrentHashMap

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

    val pluginContainer = ConcurrentHashMap<String, OpenContainer>()

    override fun getOpenContainers(): List<OpenContainer> {
        return Bukkit.getPluginManager().plugins.filter { it.javaClass.name.endsWith("platform.BukkitPlugin") && it.name != pluginId }.mapNotNull {
            pluginContainer.computeIfAbsent(it.name) { _ -> BukkitContainer(it) }
        }.filter {
            it.isValid
        }
    }

    override fun getOpenContainer(name: String): OpenContainer? {
        if (pluginContainer.containsKey(name)) {
            return pluginContainer[name]
        }
        val plugin = Bukkit.getPluginManager().getPlugin(name)
        if (plugin != null && plugin.javaClass.name.endsWith("platform.BukkitPlugin")) {
            val container = BukkitContainer(plugin)
            pluginContainer[name] = container
            return container
        }
        return null
    }
}
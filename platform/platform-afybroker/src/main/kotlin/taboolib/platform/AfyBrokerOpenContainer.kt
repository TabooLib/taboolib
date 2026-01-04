package taboolib.platform

import net.afyer.afybroker.server.Broker
import taboolib.common.Inject
import taboolib.common.OpenContainer
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.service.PlatformOpenContainer
import taboolib.platform.type.AfyBrokerContainer
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.platform.BungeeAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@Inject
@PlatformSide(Platform.AFYBROKER)
class AfyBrokerOpenContainer : PlatformOpenContainer {

    val pluginContainer = ConcurrentHashMap<String, OpenContainer>()

    override fun getOpenContainers(): List<OpenContainer> {
        return Broker.getPluginManager().plugins.filter { it.javaClass.name.endsWith("platform.AfyBrokerPlugin") && it.description.name != pluginId }.mapNotNull {
            pluginContainer.computeIfAbsent(it.description.name) { _ -> AfyBrokerContainer(it) }
        }.filter {
            it.isValid
        }
    }

    override fun getOpenContainer(name: String): OpenContainer? {
        if (pluginContainer.containsKey(name)) {
            return pluginContainer[name]
        }
        val plugin = Broker.getPluginManager().getPlugin(name)
        if (plugin != null && plugin.javaClass.name.endsWith("platform.AfyBrokerPlugin")) {
            val container = AfyBrokerContainer(plugin)
            pluginContainer[name] = container
            return container
        }
        return null
    }
}
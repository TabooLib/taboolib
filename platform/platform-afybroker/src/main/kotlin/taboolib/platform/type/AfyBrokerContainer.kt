package taboolib.platform.type

import net.afyer.afybroker.server.plugin.Plugin
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.OpenContainer
import taboolib.common.OpenResult

/**
 * TabooLib
 * taboolib.platform.type.AfyBrokerContainer
 *
 * @author Ling556
 * @since 2024/5/09 23:51
 */
class AfyBrokerContainer(plugin: Plugin) : OpenContainer {

    private val name = plugin.description.name
    private val main = plugin.description.main!!
    private val clazz = try {
        Class.forName(main.substring(0, main.length - "platform.AfyBrokerPlugin".length) + "common.OpenAPI")
    } catch (ignored: Throwable) {
        null
    }

    override fun getName(): String {
        return name
    }

    override fun call(name: String, args: Array<Any>): OpenResult {
        return try {
            OpenResult.deserialize(clazz?.invokeMethod<Any>("call", name, args, isStatic = true, remap = false) ?: return OpenResult.failed())
        } catch (ignored: NoSuchMethodException) {
            OpenResult.failed()
        }
    }
}
package taboolib.platform.type

import net.md_5.bungee.api.plugin.Plugin
import taboolib.common.OpenContainer
import taboolib.common.OpenResult
import org.tabooproject.reflex.Reflex.Companion.invokeMethod

/**
 * TabooLib
 * taboolib.platform.type.BungeeOpenContainer
 *
 * @author sky
 * @since 2021/7/3 1:44 上午
 */
class BungeeOpenContainer(plugin: Plugin) : OpenContainer {

    private val name = plugin.description.name
    private val main = plugin.description.main!!
    private val clazz = try {
        Class.forName(main.substring(0, main.length - "platform.BungeePlugin".length) + "common.OpenAPI")
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
package taboolib.platform.type

import net.md_5.bungee.api.plugin.Plugin
import taboolib.common.OpenContainer
import taboolib.common.OpenResult
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.invokeMethod

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
        val result = clazz?.invokeMethod<Any>("call", name, args, fixed = true) ?: return OpenResult.failed()
        return OpenResult(result.getProperty("successful")!!, result.getProperty("value"))
    }
}
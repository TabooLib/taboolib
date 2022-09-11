package taboolib.platform.type

import org.bukkit.plugin.Plugin
import taboolib.common.OpenContainer
import taboolib.common.OpenResult
import org.tabooproject.reflex.Reflex.Companion.invokeMethod

/**
 * TabooLib
 * taboolib.platform.type.BukkitOpenContainer
 *
 * @author sky
 * @since 2021/7/3 1:44 上午
 */
class BukkitOpenContainer(plugin: Plugin) : OpenContainer {

    private val name = plugin.name
    private val main = plugin.description.main
    private val clazz = try {
        Class.forName(main.substring(0, main.length - "platform.BukkitPlugin".length) + "common.OpenAPI")
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
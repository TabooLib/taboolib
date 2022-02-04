package taboolib.platform.type

import org.bukkit.plugin.Plugin
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.internal.Internal
import taboolib.common.OpenContainer
import taboolib.common.OpenResult

/**
 * TabooLib
 * taboolib.platform.type.BukkitOpenContainer
 *
 * @author sky
 * @since 2021/7/3 1:44 上午
 */
@Internal
class BukkitOpenContainer(plugin: Plugin) : OpenContainer {

    private val name = plugin.name
    private val main = plugin.description.main

    private val openAPI = try {
        Class.forName(main.substring(0, main.length - "platform.BukkitPlugin".length) + "common.OpenAPI")
    } catch (ignored: Throwable) {
        null
    }

    override fun getName(): String {
        return name
    }

    override fun call(name: String, args: Array<Any>): OpenResult {
        return try {
            OpenResult.deserialize(openAPI?.invokeMethod<Any>("call", name, args, isStatic = true) ?: return OpenResult.failed())
        } catch (ignored: NoSuchMethodException) {
            OpenResult.failed()
        }
    }
}
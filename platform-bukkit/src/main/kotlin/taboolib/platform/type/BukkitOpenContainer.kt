package taboolib.platform.type

import org.bukkit.plugin.Plugin
import taboolib.common.OpenContainer
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * TabooLib
 * taboolib.platform.type.BukkitOpenContainer
 *
 * @author sky
 * @since 2021/7/3 1:44 上午
 */
class BukkitOpenContainer(val plugin: Plugin) : OpenContainer {

    val main = plugin.description.main
    val clazz: Class<*> = Class.forName(main.substring(0, main.length - "platform.BukkitPlugin".length) + "common.OpenAPI")

    override fun getName(): String {
        return plugin.name
    }

    override fun register(name: String, any: ByteArray, args: Array<String>) {
        clazz.invokeMethod<Void>("register", name, any, args, fixed = true)
    }

    override fun unregister(name: String, any: ByteArray, args: Array<String>) {
        clazz.invokeMethod<Void>("unregister", name, any, args, fixed = true)
    }
}
package taboolib.platform.type

import cn.nukkit.plugin.Plugin
import taboolib.common.OpenContainer
import taboolib.common.reflect.Reflex.Companion.staticInvoke

/**
 * TabooLib
 * taboolib.platform.type.NukkitOpenContainer
 *
 * @author sky
 * @since 2021/7/3 1:44 上午
 */
class NukkitOpenContainer(val plugin: Plugin): OpenContainer {

    val main = plugin.description.main!!
    val clazz: Class<*> = Class.forName(main.substring(0, main.length - "platform.NukkitPlugin".length) + "common.OpenAPI")

    override fun getName(): String {
        return plugin.description.name
    }

    override fun register(any: Any) {
        clazz.staticInvoke<Void>("register", any)
    }

    override fun unregister(any: Any) {
        clazz.staticInvoke<Void>("unregister", any)
    }
}
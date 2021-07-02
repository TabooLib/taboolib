package taboolib.platform.type

import com.velocitypowered.api.plugin.PluginContainer
import taboolib.common.OpenContainer
import taboolib.common.reflect.Reflex.Companion.staticInvoke

/**
 * TabooLib
 * taboolib.platform.type.SpongeOpenContainer
 *
 * @author sky
 * @since 2021/7/3 1:44 上午
 */
class VelocityOpenContainer(val plugin: PluginContainer): OpenContainer {

    val main: String = plugin.instance.get().javaClass.name
    val clazz: Class<*> = Class.forName(main.substring(0, main.length - "platform.SpongePlugin".length) + "common.OpenAPI")

    override fun getName(): String {
        return plugin.description.id
    }

    override fun register(any: Any) {
        clazz.staticInvoke<Void>("register", any)
    }

    override fun unregister(any: Any) {
        clazz.staticInvoke<Void>("unregister", any)
    }
}
package taboolib.platform.type

import org.spongepowered.plugin.PluginContainer
import taboolib.common.OpenContainer
import taboolib.common.reflect.Reflex.Companion.staticInvoke

/**
 * TabooLib
 * taboolib.platform.type.SpongeOpenContainer
 *
 * @author sky
 * @since 2021/7/3 1:44 上午
 */
class Sponge8OpenContainer(val plugin: PluginContainer): OpenContainer {

    val main: String = plugin.instance().javaClass.name
    val clazz: Class<*> = Class.forName(main.substring(0, main.length - "platform.SpongePlugin".length) + "common.OpenAPI")

    override fun getName(): String {
        return plugin.metadata().id()
    }

    override fun register(name: String, any: ByteArray, args: Array<String>) {
        clazz.staticInvoke<Void>("register", name, any, args)
    }

    override fun unregister(name: String, any: ByteArray, args: Array<String>) {
        clazz.staticInvoke<Void>("unregister", name, any, args)
    }
}
package taboolib.platform.type

import org.spongepowered.api.plugin.PluginContainer
import taboolib.common.OpenContainer
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * TabooLib
 * taboolib.platform.type.SpongeOpenContainer
 *
 * @author sky
 * @since 2021/7/3 1:44 上午
 */
class Sponge7OpenContainer(val plugin: PluginContainer): OpenContainer {

    val main: String = plugin.instance.get().javaClass.name
    val clazz: Class<*> = Class.forName(main.substring(0, main.length - "platform.Sponge7Plugin".length) + "common.OpenAPI")

    override fun getName(): String {
        return plugin.id
    }

    override fun register(name: String, any: ByteArray, args: Array<String>) {
        clazz.invokeMethod<Void>("register", name, any, args, fixed = true)
    }

    override fun unregister(name: String, any: ByteArray, args: Array<String>) {
        clazz.invokeMethod<Void>("unregister", name, any, args, fixed = true)
    }
}
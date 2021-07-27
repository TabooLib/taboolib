package taboolib.platform.type

import com.velocitypowered.api.plugin.PluginContainer
import taboolib.common.OpenContainer
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * TabooLib
 * taboolib.platform.type.VelocityOpenContainer
 *
 * @author sky
 * @since 2021/7/3 1:44 上午
 */
class VelocityOpenContainer(val plugin: PluginContainer): OpenContainer {

    val main: String = plugin.instance.get().javaClass.name
    val clazz: Class<*> = Class.forName(main.substring(0, main.length - "platform.VelocityPlugin".length) + "common.OpenAPI")

    override fun getName(): String {
        return plugin.description.id
    }

    override fun register(name: String, any: ByteArray, args: Array<String>) {
        clazz.invokeMethod<Void>("register", name, any, args, fixed = true)
    }

    override fun unregister(name: String, any: ByteArray, args: Array<String>) {
        clazz.invokeMethod<Void>("unregister", name, any, args, fixed = true)
    }
}
package taboolib.platform

import de.dytanic.cloudnet.driver.module.IModule
import taboolib.common.OpenContainer
import taboolib.common.OpenResult
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.internal.Internal

/**
 * TabooLib
 * taboolib.platform.type.BungeeOpenContainer
 *
 * @author sky
 * @since 2021/7/3 1:44 上午
 */
@Internal
class CloudNetV3Container(plugin: IModule) : OpenContainer {

    private val name = plugin.name
    private val main = plugin.moduleConfig.main!!

    private val clazz = try {
        Class.forName(main.substring(0, main.length - "platform.CloudNetV3Plugin".length) + "common.OpenAPI")
    } catch (ignored: Throwable) {
        null
    }

    override fun getName(): String {
        return name
    }

    override fun call(name: String, args: Array<Any>): OpenResult {
        return try {
            OpenResult.deserialize(
                clazz?.invokeMethod<Any>("call", name, args, isStatic = true) ?: return OpenResult.failed()
            )
        } catch (ignored: NoSuchMethodException) {
            OpenResult.failed()
        }
    }
}
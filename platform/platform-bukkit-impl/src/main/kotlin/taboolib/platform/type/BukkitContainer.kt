package taboolib.platform.type

import org.bukkit.plugin.Plugin
import org.tabooproject.reflex.Reflex.Companion.invokeLocalMethod
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.OpenContainer
import taboolib.common.OpenResult
import taboolib.common.io.groupId
import taboolib.common.io.isDebugMode
import taboolib.common.io.taboolibId

/**
 * TabooLib
 * taboolib.platform.type.BukkitOpenContainer
 *
 * @author sky
 * @since 2021/7/3 1:44 上午
 */
class BukkitContainer(val plugin: Plugin) : OpenContainer {

    // 获取目标插件的 OpenAPI 类
    private val api = try {
        Class.forName("${plugin::class.java.groupId}.${taboolibId}.common.OpenAPI")
    } catch (ex: ClassNotFoundException) {
        // 在调试模式下输出错误信息
        if (isDebugMode) ex.printStackTrace()
        null
    }

    override fun isValid(): Boolean {
        return api != null
    }

    override fun getName(): String {
        return plugin.name
    }

    override fun call(name: String, args: Array<Any>): OpenResult {
        return try {
            OpenResult.cast(api?.invokeMethod<Any>("call", name, args, isStatic = true, remap = false) ?: error("OpenAPI not found in ${getName()}"))
        } catch (ex: NoSuchMethodException) {
            if (isDebugMode) ex.printStackTrace()
            OpenResult.failed()
        }
    }
}
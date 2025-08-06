package taboolib.platform

import org.bukkit.Bukkit
import taboolib.common.Inject
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformIO
import taboolib.module.chat.colored
import taboolib.module.chat.component
import taboolib.module.nms.MinecraftVersion
import java.io.File

/**
 * TabooLib
 * taboolib.platform.BukkitIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@Inject
@PlatformSide(Platform.BUKKIT)
class BukkitIO : PlatformIO {

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    override val pluginId: String
        get() = plugin.description.name

    override val pluginVersion: String
        get() = plugin.description.version

    override val isPrimaryThread: Boolean
        get() = Bukkit.isPrimaryThread()

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return Bukkit.getServer() as T
    }

    override fun info(vararg message: Any?) {
        message.filterNotNull().forEach { 
            if (isPaperComponentLogger()) {
                logWithComponent("INFO", it.toString())
            } else {
                plugin.logger.info(it.toString())
            }
        }
    }

    override fun severe(vararg message: Any?) {
        message.filterNotNull().forEach { 
            if (isPaperComponentLogger()) {
                logWithComponent("SEVERE", it.toString())
            } else {
                plugin.logger.severe(it.toString())
            }
        }
    }

    override fun warning(vararg message: Any?) {
        message.filterNotNull().forEach { 
            if (isPaperComponentLogger()) {
                logWithComponent("WARNING", it.toString())
            } else {
                plugin.logger.warning(it.toString())
            }
        }
    }

    override fun releaseResourceFile(source: String, target: String, replace: Boolean): File {
        val file = File(getDataFolder(), target)
        if (file.exists() && !replace) {
            return file
        }
        newFile(file).writeBytes(javaClass.classLoader.getResourceAsStream(source)?.readBytes() ?: error("resource not found: $source"))
        return file
    }

    override fun getJarFile(): File {
        return BukkitPlugin.getPluginInstance()?.nativeJarFile() ?: plugin.file
    }

    override fun getDataFolder(): File {
        return BukkitPlugin.getPluginInstance()?.nativeDataFolder() ?: plugin.dataFolder
    }

    override fun getPlatformData(): Map<String, Any> {
        return mapOf(
            "bukkitVersion" to Bukkit.getVersion(),
            "bukkitName" to Bukkit.getName(),
            "onlineMode" to if (Bukkit.getOnlineMode()) 1 else 0
        )
    }

    /**
     * 检查是否为支持 Component Logger 的 Paper 环境
     * Paper 1.20.6+ 不再支持传统颜色代码，需要使用 Component 系统
     */
    private fun isPaperComponentLogger(): Boolean {
        return MinecraftVersion.isUniversalCraftBukkit && MinecraftVersion.majorLegacy >= 12006
    }

    /**
     * 使用 Component 系统记录日志，保留插件消息头
     * 适用于 Paper 1.20.6+ 环境
     */
    private fun logWithComponent(level: String, message: String) {
        try {
            // 构建带插件名称前缀的消息
            val pluginPrefix = "[${plugin.description.name}]"
            val fullMessage = "$pluginPrefix $message"
            
            // 使用 TabooLib 的 component 系统处理颜色并发送到控制台
            fullMessage.component().buildColored().sendTo(taboolib.common.platform.function.console())
        } catch (e: Exception) {
            // 如果 component 系统出错，降级使用传统方式
            when (level) {
                "SEVERE" -> plugin.logger.severe(message)
                "WARNING" -> plugin.logger.warning(message)
                else -> plugin.logger.info(message)
            }
        }
    }
}
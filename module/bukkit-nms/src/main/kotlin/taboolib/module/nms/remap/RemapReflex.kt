package taboolib.module.nms.remap

import org.tabooproject.reflex.ReflexRemapper
import taboolib.common.io.isDevelopmentMode
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.module.nms.MinecraftVersion
import taboolib.platform.bukkit.Exchanges
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.remap.RemapReflex
 *
 * @author 坏黑
 * @since 2024/7/21 17:56
 */
abstract class RemapReflex : ReflexRemapper {

    // 环境信息
    val isUniversal = MinecraftVersion.isUniversal
    val major = MinecraftVersion.major

    // 缓存信息
    val fieldRemapCacheMap = Exchanges.getOrPut("reflex_remap_field_cache") { ConcurrentHashMap<String, String>() }
    val methodRemapCacheMap = Exchanges.getOrPut("reflex_remap_method_cache") { ConcurrentHashMap<String, String>() }
    val descriptorTypeCacheMap = Exchanges.getOrPut("reflex_remap_descriptor_type_cache") { ConcurrentHashMap<String, List<Class<*>>>() }

    // 映射信息
    val spigotMapping = MinecraftVersion.spigotMapping
    val paperMapping = MinecraftVersion.paperMapping

    init {
        if (isDevelopmentMode) {
            newFile(getDataFolder(), ".dev/remap.txt").delete()
        }
    }

    fun saveField(namespace: String, old: String, new: String) {
        fieldRemapCacheMap[namespace] = new
        // 开发者模式下保存映射信息
        if (isDevelopmentMode) {
            newFile(getDataFolder(), ".dev/remap.txt").appendText("f\t$namespace\t$old\t$new\n")
        }
    }

    fun saveMethod(namespace: String, old: String, new: String, descriptor: String?) {
        methodRemapCacheMap[namespace] = new
        // 开发者模式下保存映射信息
        if (isDevelopmentMode) {
            newFile(getDataFolder(), ".dev/remap.txt").appendText("m\t$namespace\t$old\t$new\t$descriptor\n")
        }
    }
}
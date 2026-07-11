package taboolib.module.nms

import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common.platform.function.warning
import taboolib.common.util.orNull
import taboolib.common.util.t
import taboolib.platform.bukkit.Exchanges
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.PacketImpl
 *
 * @author 坏黑
 * @since 2023/2/2 17:57
 */
class PacketImpl(override var source: Any) : Packet() {

    /** 数据包名称 */
    override var name = source.javaClass.simpleName.toString()

    /** 数据包名称（强制 Spigot 译名）*/
    override val nameInSpigot: String?
        get() {
            // 如果不是 Paper 服务器，或非混淆服务端，则直接返回原名称
            if (MinecraftVersion.isUnobfuscated || !MinecraftVersion.isMojangMapping) return name
            // 借助映射表获取并缓存译名
            if (spigotNameCache.containsKey(fullyName)) {
                return spigotNameCache[fullyName]!!.orNull()
            }
            // JVM 合成 Lambda / 合成类不会出现在 Spigot 映射表中（如配置阶段 UnconfiguredPipelineHandler$Lambda）
            if (isUnmappedSyntheticClass()) {
                spigotNameCache[fullyName] = Optional.empty()
                return null
            }
            val find = MinecraftVersion.paperMapping.classMapMojangToSpigot[fullyName]?.substringAfterLast('.')
            if (find == null) {
                warning(
                    """
                        未能找到 $fullyName 的 Spigot 译名。
                        Cannot find spigot name for $fullyName.
                    """.t()
                )
            }
            spigotNameCache[fullyName] = Optional.ofNullable(find)
            return find
        }

    /** 数据包名称（强制 Mojang 译名）*/
    override val nameInMojang: String?
        get() {
            // 如果是 Paper 服务器，或非混淆服务端，则直接返回原名称
            if (MinecraftVersion.isUnobfuscated || MinecraftVersion.isMojangMapping) return name
            // 借助映射表获取并缓存译名
            if (mojangNameCache.containsKey(fullyName)) {
                return mojangNameCache[fullyName]!!.orNull()
            }
            // JVM 合成 Lambda / 合成类不会出现在 Mojang 映射表中
            if (isUnmappedSyntheticClass()) {
                mojangNameCache[fullyName] = Optional.empty()
                return null
            }
            // 1.16 及以下版本，尝试获取 Spigot 译名
            val realFullyName = if (!MinecraftVersion.isUniversal)
                MinecraftVersion.spigotMapping.classMapSpigotS2F[name] ?: fullyName
            else fullyName
            val find = MinecraftVersion.paperMapping.classMapSpigotToMojang[realFullyName]?.substringAfterLast('.')
            if (find == null) {
                warning(
                    """
                        未能找到 $fullyName 的 Mojang 译名。
                        Cannot find spigot name for $fullyName.
                    """.t()
                )
            }
            mojangNameCache[fullyName] = Optional.ofNullable(find)
            return find
        }

    /** 数据包完整名称 */
    override var fullyName = source.javaClass.name.toString()

    /**
     * 判断是否为无需映射的 JVM 合成类（Lambda、匿名类等）。
     * 配置阶段 Netty 会把这类对象送进 Channel，映射表无对应项。
     */
    fun isUnmappedSyntheticClass(): Boolean {
        // HotSpot Lambda 类名形如 Outer$Lambda/0x...
        return source.javaClass.isSynthetic || fullyName.contains("$\$Lambda")
    }

    /** 读取字段 */
    override fun <T> read(name: String, remap: Boolean): T? {
        return source.getProperty<T>(name, remap = remap)
    }

    /** 写入字段 */
    override fun write(name: String, value: Any?, remap: Boolean) {
        source.setProperty(name, value, remap = remap)
    }

    /** 覆盖原始数据包 */
    override fun overwrite(newPacket: Any) {
        source = newPacket
        name = newPacket.javaClass.simpleName.toString()
        fullyName = newPacket.javaClass.name.toString()
    }

    companion object {

        val spigotNameCache = Exchanges.getOrPut("packet_spigot_name_cache") { ConcurrentHashMap<String, Optional<String>>() }
        val mojangNameCache = Exchanges.getOrPut("packet_mojang_name_cache") { ConcurrentHashMap<String, Optional<String>>() }
    }
}
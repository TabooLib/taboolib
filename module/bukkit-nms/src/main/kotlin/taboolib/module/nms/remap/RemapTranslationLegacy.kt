package taboolib.module.nms.remap

import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureWriter
import taboolib.module.nms.MinecraftVersion
import java.util.concurrent.ConcurrentHashMap

/**
 * 旧版本转译器
 *
 * TabooLib
 * taboolib.module.nms.remap.MinecraftRemapper
 *
 * @author sky
 * @since 2021/7/17 2:02 上午
 */
open class RemapTranslationLegacy : RemapTranslation() {

    /**
     * 缓存类的父类和接口
     */
    val parentsCacheMap = ConcurrentHashMap<String, List<String>>()

    /**
     * 在 1.17 版本下进行字段转换
     *
     * $owner.$name
     * net/minecraft/server/level/EntityPlayer.connection -> b
     */
    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        if (MinecraftVersion.isUniversal) {
            // 当前运行时的 Owner 名称
            val runningOwner = translate(owner).replace('/', '.')
            // 追溯父类和接口
            val findPath = parentsCacheMap.getOrPut(runningOwner) { findParents(runningOwner).reversed() }
            // 这里肯定是非 Universal CraftBukkit 环境
            // 先尝试当作 Mojang Deobf 转为 Mojang obf，否则当作 Spigot Deobf 转为 Mojang obf
            // 这里有个好处是：Spigot 方法和字段的映射在 1.18+ 才提供，但是 Mojang Deobf 从 1.17 开始就提供了，可以直接使用
            var mojangName = MinecraftVersion.paperMapping.fields.find { it.translateName == name && it.path in findPath }?.mojangName
            if (mojangName == null) {
                mojangName = MinecraftVersion.spigotMapping.fields.find { it.translateName == name && it.path in findPath }?.mojangName
            }
            return mojangName ?: name
        }
        return name
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        // 1.18
        if (MinecraftVersion.major >= 10) {
            // 为什么这么做？
            // 以 send(Packet) 函数为例，除了 send 需要转译之外，Packet 也需要。
            val signatureWriter = object : SignatureWriter() {
                override fun visitClassType(name: String) {
                    super.visitClassType(translate(name))
                }
            }
            SignatureReader(descriptor).accept(signatureWriter)
            val desc = signatureWriter.toString()
            // 当前运行时的 Owner 名称
            val runningOwner = translate(owner).replace('/', '.')
            // 追溯父类和接口
            val findPath = parentsCacheMap.getOrPut(runningOwner) { findParents(runningOwner).reversed() }
            // 这里肯定是非 Universal CraftBukkit 环境
            // 先尝试当作 Mojang Deobf 转为 Mojang obf，否则当作 Spigot Deobf 转为 Mojang obf
            // 这里有个好处是：Spigot 方法和字段的映射在 1.18+ 才提供，但是 Mojang Deobf 从 1.17 开始就提供了，可以直接使用
            var mojangName = MinecraftVersion.paperMapping.methods.find {
                // 根据复杂程度依次对比
                it.translateName == name && it.path in findPath && RemapHelper.checkParameterType(desc, it.descriptor)
            }?.mojangName
            if (mojangName == null) {
                mojangName = MinecraftVersion.spigotMapping.methods.find {
                    // 根据复杂程度依次对比
                    it.translateName == name && it.path in findPath && RemapHelper.checkParameterType(desc, it.descriptor)
                }?.mojangName
            }
            return mojangName ?: name
        }
        return name
    }
}
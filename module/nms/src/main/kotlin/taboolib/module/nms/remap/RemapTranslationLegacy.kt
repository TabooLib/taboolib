package taboolib.module.nms.remap

import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureWriter
import taboolib.module.nms.MinecraftVersion
import java.util.concurrent.ConcurrentHashMap

/**
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
            return MinecraftVersion.spigotMapping.fields.find { it.translateName == name && it.path in findPath }?.mojangName ?: name
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
            return MinecraftVersion.spigotMapping.methods.find {
                // 根据复杂程度依次对比
                it.translateName == name
                        && it.path in findPath
                        && RemapHelper.checkParameterType(desc, it.descriptor)
            }?.mojangName ?: name
        }
        return name
    }
}
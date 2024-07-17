package taboolib.module.nms

import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureWriter
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.MinecraftRemapper
 *
 * @author sky
 * @since 2021/7/17 2:02 上午
 */
open class MinecraftRemapper : Remapper() {

    /**
     * 只匹配 1.17 之下的 nms 包名
     */
    val nms1 = "net/minecraft/server/v1_.*?/".toRegex()
    val nms2 = "net/minecraft/server/${MinecraftVersion.minecraftVersion}/"

    // FIXME 此方式将在 Paper 1.20.5 中失效
    val obc1 = "org/bukkit/craftbukkit/v1_.*?/".toRegex()

    // FIXME 此方式将在 Paper 1.20.5 中失效
    val obc2 = "org/bukkit/craftbukkit/${MinecraftVersion.minecraftVersion}/"

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
            val findPath = parentsCacheMap.getOrPut(runningOwner) { findParents(runningOwner).reversed() }
            return MinecraftVersion.mapping.fields.find { it.path in findPath && it.translateName == name }?.mojangName ?: name
        }
        return name
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        // 1.18
        if (MinecraftVersion.major >= 10) {
            val signatureWriter = object : SignatureWriter() {
                override fun visitClassType(name: String) {
                    super.visitClassType(translate(name))
                }
            }
            SignatureReader(descriptor).accept(signatureWriter)
            val desc = signatureWriter.toString()
            // 当前运行时的 Owner 名称
            val runningOwner = translate(owner).replace('/', '.')
            val findPath = parentsCacheMap.getOrPut(runningOwner) { findParents(runningOwner).reversed() }
            return MinecraftVersion.mapping.methods.find { it.path in findPath && it.translateName == name && it.descriptor == desc }?.mojangName ?: name
        }
        return name
    }

    override fun mapType(internalName: String): String {
        return super.mapType(translate(internalName))
    }

    override fun map(internalName: String): String {
        return translate(internalName)
    }

    /**
     * 包名转换方法
     */
    fun translate(key: String): String {
        // obc
        // FIXME 此方式将在 Paper 1.20.5 中失效
        if (key.startsWith("org/bukkit/craftbukkit")) {
            return key.replace(obc1, obc2)
        }
        // 统一版本
        return if (MinecraftVersion.isUniversal) {
            // 将低版本包名替换为高版本包名
            // net/minecraft/server/v1_17_R1/EntityPlayer -> net/minecraft/server/level/EntityPlayer
            if (key.startsWith("net/minecraft/server/v1_")) {
                MinecraftVersion.mapping.classMap[key.substringAfterLast('/', "")]?.replace('.', '/') ?: key
            } else {
                key
            }
        } else {
            // 将高版本包名替换为低版本包名
            // net/minecraft/server/level/EntityPlayer -> net/minecraft/server/v1_17_R1/EntityPlayer
            if (MinecraftVersion.mapping.classMap.containsValue(key.replace('.', '/'))) {
                "net/minecraft/server/${MinecraftVersion.minecraftVersion}/${key.substringAfterLast('/', "")}"
            } else {
                key.replace(nms1, nms2)
            }
        }
    }

    /**
     * 获取类的所有父类和接口
     */
    fun findParents(owner: String): Set<String> {
        if (owner.startsWith("net.minecraft") || owner.startsWith("com.mojang")) {
            try {
                val find = hashSetOf<String>()
                find += owner
                val forName = Class.forName(owner)
                find += forName.interfaces.map { it.name }
                val superclass = forName.superclass
                if (superclass != null && superclass.name != "java.lang.Object") {
                    find += findParents(superclass.name)
                }
                return find
            } catch (_: Throwable) {
            }
        }
        return hashSetOf(owner)
    }
}
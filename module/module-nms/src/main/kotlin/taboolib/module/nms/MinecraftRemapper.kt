package taboolib.module.nms

import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureWriter

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
    val obc1 = "org/bukkit/craftbukkit/v1_.*?/".toRegex()
    val obc2 = "org/bukkit/craftbukkit/${MinecraftVersion.minecraftVersion}/"

    val mapping by lazy {
        MinecraftVersion.mapping
    }

    /**
     * 在 1.17 版本下进行字段转换
     *
     * $owner.$name
     * net/minecraft/server/level/EntityPlayer.connection -> b
     */
    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        if (MinecraftVersion.isUniversal) {
            val universal = translate(owner).replace('/', '.')
            return mapping.fields.firstOrNull { it.path == universal && it.translateName == name }?.mojangName ?: name
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
            val universal = translate(owner).replace('/', '.')
            return mapping.methods.firstOrNull { it.path == universal && it.translateName == name && it.descriptor == desc }?.mojangName ?: name
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
        if (key.startsWith("org/bukkit/craftbukkit")) {
            return key.replace(obc1, obc2)
        }
        // 统一版本
        return if (MinecraftVersion.isUniversal) {
            // 将低版本包名替换为高版本包名
            // net/minecraft/server/v1_17_R1/EntityPlayer -> net/minecraft/server/level/EntityPlayer
            if (key.startsWith("net/minecraft/server/v1_")) {
                mapping.classMap[key.substringAfterLast('/', "")]?.replace('.', '/') ?: key
            } else {
                key
            }
        } else {
            // 将高版本包名替换为低版本包名
            // net/minecraft/server/level/EntityPlayer -> net/minecraft/server/v1_17_R1/EntityPlayer
            if (mapping.classMap.containsValue(key.replace('/', '.'))) {
                "net/minecraft/server/${MinecraftVersion.minecraftVersion}/${key.substringAfterLast('/', "")}"
            } else {
                key.replace(nms1, nms2)
            }
        }
    }
}
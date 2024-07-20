package taboolib.module.nms.remap

import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureWriter
import taboolib.module.nms.MinecraftVersion

/**
 * TabooLib
 * taboolib.module.nms.remap.RemapTranslationTabooLib
 *
 * 只有 Paper 1.20.6+ 才会启用该类用于转译 TabooLib 类。
 * 与 RemapTranslation 不同的是，此实现不会进行父类检索。
 *
 * @author 坏黑
 * @since 2024/7/21 04:13
 */
class RemapTranslationTabooLib : Remapper() {

    val obc1 = "org/bukkit/craftbukkit/v1_.*?/".toRegex()
    val obc2 = "org/bukkit/craftbukkit/"

    val descriptorCache = HashMap<String, String>()

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        val ownerName = owner.replace('/', '.')
        // 从 Spigot Mapping 中检索
        for (spigotField in MinecraftVersion.spigotMapping.fields) {
            // 类名符合
            if (spigotField.path == ownerName) {
                // 获取用于在 Mojang Mapping 中检索的名字（已还原为 Mojang Obf）
                val obf = if (spigotField.translateName == name || spigotField.mojangName == name) {
                    spigotField.mojangName
                } else {
                    continue // 什么情况会这样？同类但不同字段
                }
                // 将类名转换为 Mojang Deobf
                val mojangName = translate(owner).replace('/', '.')
                // 从 Mojang Mapping 中检索
                for (mojangField in MinecraftVersion.paperMapping.fields) {
                    if (mojangField.mojangName == obf && mojangField.path == mojangName) {
                        // 最终返回 Mojang Deobf 名
                        return mojangField.translateName
                    }
                }
            }
        }
        return name
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        val ownerName = owner.replace('/', '.')
        // 从 Spigot Mapping 中检索
        for (spigotMethod in MinecraftVersion.spigotMapping.methods) {
            // 类名符合
            if (spigotMethod.path == ownerName) {
                // 为什么这么做？
                // 以 send(Packet) 函数为例，除了 send 需要转译之外，Packet 也需要。
                val desc = descriptorCache.getOrPut(descriptor) {
                    val signatureWriter = object : SignatureWriter() {
                        override fun visitClassType(name: String) {
                            super.visitClassType(translate(name))
                        }
                    }
                    SignatureReader(descriptor).accept(signatureWriter)
                    signatureWriter.toString()
                }
                // 获取用于在 Mojang Mapping 中检索的名字（已还原为 Mojang Obf）
                val obf = if (spigotMethod.translateName == name || spigotMethod.mojangName == name) {
                    // 与字段不同的是，方法需要额外判断描述符
                    if (spigotMethod.descriptor == desc) spigotMethod.mojangName
                    else continue
                } else {
                    continue
                }
                // 将类名转换为 Mojang Deobf
                val mojangName = translate(owner).replace('/', '.')
                // 从 Mojang Mapping 中检索
                for (mojangMethod in MinecraftVersion.paperMapping.methods) {
                    if (mojangMethod.mojangName == obf && mojangMethod.path == mojangName && mojangMethod.descriptor == desc) {
                        // 最终返回 Mojang Deobf 名
                        return mojangMethod.translateName
                    }
                }
            }
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
        return MinecraftVersion.paperMapping.classLookupByMojangMapping[key.replace('/', '.')]?.replace('.', '/') ?: key
    }
}
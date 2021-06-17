package taboolib.module.nms

import org.objectweb.asm.commons.SimpleRemapper

/**
 * TabooLib
 * taboolib.module.nms.MinecraftRemapper
 *
 * @author sky
 * @since 2021/6/18 2:02 上午
 */
class MinecraftRemapper(val map: Map<String, String>) : SimpleRemapper(map) {

    val nms1 = "net/minecraft/server/.*?/".toRegex()
    val nms2 = "net/minecraft/server/${MinecraftVersion.legacyVersion}/"
    val obc1 = "org/bukkit/craftbukkit/.*?/".toRegex()
    val obc2 = "org/bukkit/craftbukkit/${MinecraftVersion.legacyVersion}/"

    override fun map(key: String): String {
        return if (key.startsWith("org/bukkit/craftbukkit")) key.replace(obc1, obc2) else super.map(key.replace(nms1, nms2)) ?: key.replace(nms1, nms2)
    }
}
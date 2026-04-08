package taboolib.module.nms.remap

import taboolib.module.nms.MinecraftVersion

/**
 * TabooLib
 * taboolib.module.nms.remap.RemapTranslationUnobfsucated
 *
 * @author mical
 * @since 2026/3/31 22:53
 */
class RemapTranslationUnobfuscated : RemapTranslation() {

    override fun translate(key: String): String {
        // obc
        // 非混淆服务端，只能处理 obc 的版本号了
        if (key.startsWith("org/bukkit/craftbukkit")) {
            // 若当前使用 Universal CraftBukkit 环境，则移除版本号
            return key.replace(obc1, if (MinecraftVersion.isUniversalCraftBukkit) obc3 else obc2)
        }
        return key
    }
}
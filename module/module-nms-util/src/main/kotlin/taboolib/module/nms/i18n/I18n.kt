package taboolib.module.nms.i18n

import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.MinecraftVersion.major
import taboolib.module.nms.MinecraftVersion.minecraftVersion

/**
 * 原版语言文件工具
 *
 * @author sky
 * @since 2020-04-04 19:33
 */
@PlatformSide([Platform.BUKKIT])
object I18n {

    /** 当前版本的语言文件 */
    val instance: I18nBase by unsafeLazy {
        val field = when (major) {
            // 1.8 .. 1.14
            in MinecraftVersion.V1_8..MinecraftVersion.V1_14 -> I18nLegacy.INSTANCE
            // 1.15 .. 1.20
            in MinecraftVersion.V1_15..MinecraftVersion.V1_20 -> I18nCurrently
            // 其他版本
            else -> error("Unsupported version")
        }
        field.init()
        field
    }
}
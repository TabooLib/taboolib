package taboolib.module.nms.i18n

import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.module.nms.MinecraftVersion.major

/**
 * 原版语言文件工具
 *
 * @author sky
 * @since 2020-04-04 19:33
 */
@Awake
@PlatformSide([Platform.BUKKIT])
object I18n {

    val version = HashMap<Int, I18nBase>()
    val instance: I18nBase

    init {
        // 1.8 .. 1.14
        (1..6).forEach { version[it] = I18nLegacy.INSTANCE }
        // 1.15 .. 1.20
        (7..12).forEach { version[it] = I18nCurrently }
        // 获取版本
        instance = version[major] ?: error("Unsupported version")
        instance.init()
    }
}
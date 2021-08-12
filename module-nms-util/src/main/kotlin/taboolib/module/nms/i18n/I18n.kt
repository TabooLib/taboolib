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
        version[7] = I18n11700 // 1.15
        version[8] = I18n11700 // 1.16
        version[9] = I18n11700 // 1.17
        instance = version.getOrDefault(major, I18nOrigin.INSTANCE)
        instance.init()
    }
}
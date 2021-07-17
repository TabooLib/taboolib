package taboolib.module.nms.i18n

import com.google.common.collect.Maps
import taboolib.module.nms.MinecraftVersion.major
import taboolib.common.platform.Awake
import taboolib.module.nms.i18n.I18nBase
import taboolib.module.nms.i18n.I18n11700
import taboolib.module.nms.i18n.I18nOrigin

/**
 * 原版语言文件工具
 *
 * @author sky
 * @since 2020-04-04 19:33
 */
@Awake
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
package taboolib.module.nms.i18n

import taboolib.common.util.unsafeLazy

/**
 * 原版语言文件工具
 *
 * @author sky
 * @since 2020-04-04 19:33
 */
object I18n {

    /** 当前版本的语言文件 */
    val instance: I18nBase by unsafeLazy { I18nCurrently() }
}
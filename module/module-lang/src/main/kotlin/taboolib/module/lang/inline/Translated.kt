package taboolib.module.lang.inline

import taboolib.module.lang.Language

/**
 * Codex
 * taboolib.module.lang.inline.Translated
 *
 * @author 坏黑
 * @since 2024/6/15 20:47
 */
abstract class Translated<T>(val node: String, val default: T) {

    /** 获取翻译 */
    abstract fun get(locale: String = "zh_CN"): T
}
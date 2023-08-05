package taboolib.module.nms.type

/**
 * TabooLib
 * taboolib.module.nms.type.LocaleKey
 *
 * @author 坏黑
 * @since 2023/8/6 04:41
 */
data class LocaleKey(val type: String, val key: String, val extra: String? = null) {

    override fun toString(): String {
        return "[$type] " + if (extra == null) key else "$key ($extra)"
    }
}
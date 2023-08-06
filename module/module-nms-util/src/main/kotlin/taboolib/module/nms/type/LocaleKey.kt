package taboolib.module.nms.type

/**
 * 语言文件节点
 *
 * @param type 类型（N=正常，S=特殊处理，D=缺省）
 * @param key 节点
 * @param extra 额外信息（在低版本中表现为生成蛋的类型）
 */
data class LocaleKey(val type: String, val key: String, val extra: String? = null) {

    override fun toString(): String {
        return "[$type] " + if (extra == null) key else "$key ($extra)"
    }
}
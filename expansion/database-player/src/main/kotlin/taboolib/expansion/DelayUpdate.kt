package taboolib.expansion

/**
 * TabooLib
 * taboolib.expansion.DelayUpdate
 *
 * @author 坏黑
 * @since 2022/8/25 15:48
 */
class DelayUpdate(val key: String, val updateTime: Long) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DelayUpdate) return false
        if (key != other.key) return false
        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}
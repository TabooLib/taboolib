package taboolib.common.reflect

/**
 * TabooLib
 * taboolib.common.reflect.ClassDefine
 *
 * @author 坏黑
 * @since 2024/7/6 09:35
 */
class ClassMarkers(val hashCode: Int) {

    fun match(it: Class<*>): Boolean {
        return true
    }

    fun mark(it: Class<*>) {
    }
}
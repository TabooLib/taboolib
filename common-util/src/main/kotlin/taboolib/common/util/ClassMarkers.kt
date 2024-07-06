package taboolib.common.util

import java.util.function.Supplier

/**
 * TabooLib
 * taboolib.common.reflect.ClassDefine
 *
 * @author 坏黑
 * @since 2024/7/6 09:35
 */
class ClassMarkers(val version: String) {

    fun match(group: String, it: Class<*>, process: Supplier<Boolean>): Boolean {
        return true
    }
}
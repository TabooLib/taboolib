package taboolib.common.reflect

import org.tabooproject.reflex.UnsafeAccess
import java.lang.reflect.Field

@Deprecated("Use taboolib.library.reflex.UnsafeAccess")
object Ref {

    fun put(src: Any?, field: Field, value: Any?) {
        UnsafeAccess.put(src, field, value)
    }

    fun <T> get(src: Any?, field: Field): T? {
        return UnsafeAccess.get(src, field)
    }
}
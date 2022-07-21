package taboolib.common.reflect

import org.tabooproject.reflex.Reflex.Companion.getProperty as getProperty0
import org.tabooproject.reflex.Reflex.Companion.invokeConstructor as invokeConstructor0
import org.tabooproject.reflex.Reflex.Companion.invokeMethod as invokeMethod0
import org.tabooproject.reflex.Reflex.Companion.setProperty as setProperty0
import org.tabooproject.reflex.Reflex.Companion.unsafeInstance as unsafeInstance0

/**
 * @author sky
 * @since 2020-10-02 01:40
 */
@Deprecated("Use taboolib.library.reflex.Reflex")
@Suppress("DeprecatedCallableAddReplaceWith")
class Reflex {

    companion object {

        @Deprecated("Use taboolib.library.reflex.Reflex")
        fun <T> Class<T>.unsafeInstance(): Any {
            return unsafeInstance0()
        }

        @Deprecated("Use taboolib.library.reflex.Reflex")
        fun <T> Class<T>.invokeConstructor(vararg parameter: Any?): T {
            return invokeConstructor0(*parameter)
        }

        @Deprecated("Use taboolib.library.reflex.Reflex")
        fun <T> Any.invokeMethod(name: String, vararg parameter: Any?, fixed: Boolean = false): T? {
            return invokeMethod0(name, *parameter, isStatic = fixed, findToParent = true)
        }

        @Deprecated("Use taboolib.library.reflex.Reflex")
        fun <T> Any.invokeMethod(name: String, vararg parameter: Any?, fixed: Boolean = false, findToParent: Boolean = true): T? {
            return invokeMethod0(name, *parameter, isStatic = fixed, findToParent = findToParent)
        }

        @Deprecated("Use taboolib.library.reflex.Reflex")
        fun <T> Any.getProperty(path: String, fixed: Boolean = false): T? {
            return getProperty0(path, isStatic = fixed, true)
        }

        @Deprecated("Use taboolib.library.reflex.Reflex")
        fun <T> Any.getProperty(path: String, fixed: Boolean = false, findToParent: Boolean = true): T? {
            return getProperty0(path, isStatic = fixed, findToParent = findToParent)
        }

        @Deprecated("Use taboolib.library.reflex.Reflex")
        fun Any.setProperty(path: String, value: Any?, fixed: Boolean = false) {
            setProperty0(path, value, isStatic = fixed, findToParent = true)
        }

        @Deprecated("Use taboolib.library.reflex.Reflex")
        fun Any.setProperty(path: String, value: Any?, fixed: Boolean = false, findToParent: Boolean = true) {
            setProperty0(path, value, isStatic = fixed, findToParent = findToParent)
        }
    }
}
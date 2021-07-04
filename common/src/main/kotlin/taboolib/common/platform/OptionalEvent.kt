package taboolib.common.platform

import taboolib.common.reflect.Reflex.Companion.reflex

/**
 * TabooLib
 * taboolib.common.platform.OptionalEvent
 *
 * @author sky
 * @since 2021/7/4 3:19 下午
 */
class OptionalEvent(val source: Any) {

    fun <T> cast(cast: Class<T>): T {
        return cast.cast(source)!!
    }

    fun <T> read(name: String): T? {
        return source.reflex<T>(name)
    }

    fun write(name: String, value: Any?) {
        source.reflex(name, value)
    }
}
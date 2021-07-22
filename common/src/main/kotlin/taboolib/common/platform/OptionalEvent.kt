package taboolib.common.platform

import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.setProperty

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
        return source.getProperty<T>(name)
    }

    fun write(name: String, value: Any?) {
        source.setProperty(name, value)
    }
}
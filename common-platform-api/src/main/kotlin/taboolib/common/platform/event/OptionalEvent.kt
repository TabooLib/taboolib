package taboolib.common.platform.event

import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.setProperty

/**
 * TabooLib
 * taboolib.common.platform.event.OptionalEvent
 *
 * @author sky
 * @since 2021/7/4 3:19 下午
 */
class OptionalEvent(val source: Any) {

    inline fun <reified T> get(): T {
        return source as T
    }

    fun <T> read(name: String): T? {
        return source.getProperty<T>(name)
    }

    fun write(name: String, value: Any?) {
        source.setProperty(name, value)
    }
}
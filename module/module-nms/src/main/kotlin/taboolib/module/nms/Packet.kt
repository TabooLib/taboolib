package taboolib.module.nms

import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.setProperty

/**
 * TabooLib
 * taboolib.module.nms.Packet
 *
 * @author sky
 * @since 2021/6/24 5:39 下午
 */
class Packet(val source: Any) {

    val name = source.javaClass.simpleName.toString()

    val fullyName = source.javaClass.name.toString()

    fun <T> read(name: String): T? {
        return source.getProperty<T>(name)
    }

    fun write(name: String, value: Any?) {
        source.setProperty(name, value)
    }
}
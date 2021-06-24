package taboolib.module.nms

import taboolib.common.reflect.Reflex.Companion.reflex

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
        return source.reflex<T>(name)
    }

    fun write(name: String, value: Any?) {
        source.reflex(name, value)
    }
}
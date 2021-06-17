package taboolib.module.kether

import taboolib.common.platform.registerListener
import taboolib.common.platform.unregisterListener

/**
 * @Author IzzelAliz
 */
object Closables {

    @Suppress("UNCHECKED_CAST")
    fun <T> listening(clazz: Class<*>, consumer: (T) -> Unit): AutoCloseable {
        val listener = registerListener(clazz) { consumer(it as T) }
        return AutoCloseable { unregisterListener(listener) }
    }
}
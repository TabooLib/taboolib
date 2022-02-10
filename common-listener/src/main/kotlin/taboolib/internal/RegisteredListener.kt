package taboolib.internal

import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.unregisterListener
import java.io.Closeable

class RegisteredListener : Closeable {

    internal var listener: ProxyListener? = null

    override fun close() {
        unregisterListener(listener ?: error("close untimely"))
    }
}
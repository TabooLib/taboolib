package taboolib.internal

import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.platform.function.isPlatformEvent
import taboolib.common.platform.function.isProxyEvent
import java.util.concurrent.CopyOnWriteArraySet

/**
 * TabooLib
 * taboolib.internal.ActiveListener
 *
 * @author 坏黑
 * @since 2022/2/6 3:26 PM
 */
@Internal
@Suppress("UnnecessaryAbstractClass")
abstract class ActiveListener(val clazz: Class<*>, val consumer: (Any) -> Unit) {

    protected val isProxyEvent = clazz.isProxyEvent
    protected val skipEvent = CopyOnWriteArraySet<Class<*>>()

    protected fun process(event: Any) {
        if (event::class.java in skipEvent) return
        var origin: Any = event
        // 如果监听的是跨平台事件
        if (isProxyEvent) {
            // 触发事件是否为子平台代理事件
            if (event.javaClass.isPlatformEvent) {
                // 获取子平台代理事件的原始事件
                event.getProperty<Any>("proxyEvent")?.let { origin = it }
            } else {
                skipEvent += event.javaClass
                return
            }
        }
        if (clazz.isAssignableFrom(origin.javaClass)) {
            consumer(origin)
        } else {
            skipEvent += event.javaClass
        }
    }
}

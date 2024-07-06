package taboolib.common.platform.service

import taboolib.common.Inject
import taboolib.common.platform.PlatformService
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.ProxyListener

/**
 * TabooLib
 * taboolib.common.platform.service.PlatformListener
 *
 * @author sky
 * @since 2021/6/17 12:04 上午
 */
@Inject
@PlatformService
interface PlatformListener {

    /**
     * bukkit & nukkit
     */
    fun <T> registerListener(event: Class<T>, priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = true, func: (T) -> Unit): ProxyListener

    /**
     * bungeecord
     */
    fun <T> registerListener(event: Class<T>, level: Int, ignoreCancelled: Boolean = false, func: (T) -> Unit): ProxyListener = error("Unsupported")

    /**
     * velocity
     */
    fun <T> registerListener(event: Class<T>, postOrder: PostOrder = PostOrder.NORMAL, func: (T) -> Unit): ProxyListener = error("Unsupported")

    /**
     * 注销监听器
     */
    fun unregisterListener(proxyListener: ProxyListener)
}
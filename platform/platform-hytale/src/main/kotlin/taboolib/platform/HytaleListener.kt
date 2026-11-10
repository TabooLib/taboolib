package taboolib.platform

import com.hypixel.hytale.component.system.EcsEvent
import com.hypixel.hytale.component.system.ISystem
import com.hypixel.hytale.event.EventRegistration
import com.hypixel.hytale.event.IAsyncEvent
import com.hypixel.hytale.event.IBaseEvent
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.HytaleEventHandler
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.service.PlatformListener
import taboolib.common.util.unsafeLazy
import taboolib.platform.type.HytaleCommandSender
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

/**
 * TabooLib
 * taboolib.platform.HytaleListener
 *
 * @author sky
 * @since 2024/1/1
 */
@Awake
@Inject
@PlatformSide(Platform.HYTALE)
class HytaleListener : PlatformListener {

    val plugin by unsafeLazy { HytalePlugin.getInstance() }

    @Awake(LifeCycle.ENABLE)
    private fun registerPlayerDisconnectListener() {
        plugin.eventRegistry.register(PlayerDisconnectEvent::class.java, Consumer { event ->
            HytaleCommandSender.fireQuitCallbacks(event.playerRef)
        })
    }

    @Awake(LifeCycle.DISABLE)
    private fun clearPlayerQuitCallbacks() {
        HytaleCommandSender.clearQuitCallbacks()
    }

    override fun <T> registerListener(event: Class<T>, priority: EventPriority, ignoreCancelled: Boolean, func: (T) -> Unit): ProxyListener {
        error("Unsupported")
    }

    override fun <T> registerListener(event: Class<T>, postOrder: PostOrder, func: (T) -> Unit): ProxyListener {
        error("Unsupported")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerListener(event: Class<T>, handler: HytaleEventHandler<T>): ProxyListener {
        return when (handler) {
            is HytaleEventHandler.Sync -> registerSyncEvent(event, handler)
            is HytaleEventHandler.AsyncBase -> registerAsyncEvent(event, handler)
            is HytaleEventHandler.Ecs<*, *> -> registerEcsEvent(event as Class<EcsEvent>, handler as HytaleEventHandler.Ecs<EcsEvent, Any>)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> registerSyncEvent(event: Class<T>, handler: HytaleEventHandler.Sync<T>): ProxyListener {
        val registry = plugin.eventRegistry
        val priority = handler.priority
        val key = handler.key
        val eventClass = event as Class<IBaseEvent<Any>>
        val consumer = Consumer<IBaseEvent<Any>> { e -> handler.func(e as T) }
        val registration: EventRegistration<*, *>? = when (handler) {
            is HytaleEventHandler.Normal -> if (key != null) {
                registry.register(priority, eventClass, key, consumer)
            } else {
                registry.register(priority, eventClass as Class<IBaseEvent<Void>>, consumer as Consumer<IBaseEvent<Void>>)
            }
            is HytaleEventHandler.Global -> registry.registerGlobal(priority, eventClass, consumer)
            is HytaleEventHandler.Unhandled -> registry.registerUnhandled(priority, eventClass, consumer)
        }
        return HytaleProxyListener(registration ?: error("Failed to register event listener for ${event.name}"))
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> registerAsyncEvent(event: Class<T>, handler: HytaleEventHandler.AsyncBase<T>): ProxyListener {
        val registry = plugin.eventRegistry
        val priority = handler.priority
        val key = handler.key
        val eventClass = event as Class<IAsyncEvent<Any>>
        val function = Function<CompletableFuture<IAsyncEvent<Any>>, CompletableFuture<IAsyncEvent<Any>>> { future ->
            invokeAsyncHandler(
                future,
                handler.func as Function<CompletableFuture<IAsyncEvent<Any>>, CompletableFuture<IAsyncEvent<Any>>>
            )
        }
        val registration: EventRegistration<*, *>? = when (handler) {
            is HytaleEventHandler.Async -> if (key != null) {
                registry.registerAsync(priority, eventClass, key, function)
            } else {
                registry.registerAsync(priority, eventClass as Class<IAsyncEvent<Void>>, function as Function<CompletableFuture<IAsyncEvent<Void>>, CompletableFuture<IAsyncEvent<Void>>>)
            }
            is HytaleEventHandler.AsyncGlobal -> registry.registerAsyncGlobal(priority, eventClass, function)
            is HytaleEventHandler.AsyncUnhandled -> registry.registerAsyncUnhandled(priority, eventClass, function)
        }
        return HytaleProxyListener(registration ?: error("Failed to register async event listener for ${event.name}"))
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerEcsEvent(event: Class<EcsEvent>, handler: HytaleEventHandler.Ecs<EcsEvent, Any>): ProxyListener {
        val system = HytaleEcsEventSystem(event) { ctx, e -> handler.func(ctx, e) }
        plugin.entityStoreRegistry.registerSystem(system as ISystem<EntityStore>)
        return HytaleEcsProxyListener(system)
    }

    override fun unregisterListener(proxyListener: ProxyListener) {
        when (proxyListener) {
            is HytaleProxyListener -> proxyListener.registration.unregister()
            is HytaleEcsProxyListener -> {} // ECS 系统会在插件关闭时自动注销
        }
    }

    class HytaleProxyListener(val registration: EventRegistration<*, *>) : ProxyListener

    class HytaleEcsProxyListener(val system: HytaleEcsEventSystem<*>) : ProxyListener
}

@JvmSynthetic
internal fun <T> invokeAsyncHandler(
    future: CompletableFuture<T>,
    handler: Function<CompletableFuture<T>, CompletableFuture<T>>,
): CompletableFuture<T> {
    return try {
        (handler.apply(future) as CompletableFuture<T>?) ?: CompletableFuture<T>().also {
            it.completeExceptionally(NullPointerException("Async event handler returned null"))
        }
    } catch (ex: Throwable) {
        CompletableFuture<T>().also { it.completeExceptionally(ex) }
    }
}

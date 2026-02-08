package taboolib.module.nms

import io.netty.channel.Channel
import org.bukkit.entity.Player
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import taboolib.common.*
import taboolib.common.event.InternalEventBus
import taboolib.common.io.isDebugMode
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.debug
import taboolib.common.platform.function.getOpenContainers
import taboolib.common.platform.function.pluginId
import taboolib.platform.BukkitPlugin
import taboolib.platform.bukkit.Exchanges

/**
 * @author 坏黑
 * @since 2018-10-28 14:34
 */
@Inject
@Awake
@PlatformSide(Platform.BUKKIT)
object ProtocolHandler : OpenListener {

    // 数据包监听器
    const val PACKET_LISTENER = "packet_listener/v1"

    // 数据包监听器注销
    const val PACKET_LISTENER_EJECT = "packet_listener_eject/v1"

    // 数据包接收
    const val PACKET_RECEIVE = "packet_receive/v1"

    // 数据包发送
    const val PACKET_SEND = "packet_send/v1"

    /**
     * 使用 LightInjector 作为 TabooLib 数据包实现。
     * 不再对外开放，因为随着版本更新，底层实现可能会变更，在这之前曾使用 TinyProtocol。
     *
     * 这类工具现均已停止维护，可能因服务端改动频繁维护成本极高。
     * 未来可能会选择使用 retrooper/packetevents，但它是个巨无霸。
     */
    var instance: LightInjector? = null

    /**
     * 当前插件是否已经注入数据包监听器
     */
    fun isInjected(): Boolean {
        return instance != null
    }

    /**
     * 当前所有启用数据包共享的 OpenContainer 缓存
     * 当插件加载或卸载时，缓存会被更新
     */
    var containers = listOf<OpenContainer>()

    /**
     * 更新 OpenContainer 缓存
     */
    fun updateContainer() {
        containers = getOpenContainers().filter { it.name != pluginId && Exchanges.contains(PACKET_LISTENER + "/plugin/" + it.name) }
    }

    /**
     * 向其他 TabooLib 插件共享数据包事件
     *
     * @param method API 名，指定为 PACKET_RECEIVE 或 PACKET_SEND
     * @param sender 数据包发送者或接收者
     * @param channel 数据包通道
     * @param packet 数据包原始对象
     * @return 返回值将会替换原始数据包，如果为 null 则表示已被拦截
     */
    fun handlePacket(method: String, sender: Player?, channel: Channel, packet: Any): Any? {
        var current = packet
        var isCancelled = false
        containers.forEach {
            val result = it.call(method, arrayOf(sender, channel, current))
            if (result.isSuccessful) {
                current = result.value ?: current
            } else {
                isCancelled = true
                // debug("Packet ${packet.javaClass.name} cancelled by ${it.name}.")
            }
        }
        return if (isCancelled) null else current
    }


    /**
     * 当前插件是否监听了数据包事件
     */
    private fun isPacketEventListened(): Boolean {
        return InternalEventBus.isListening(PacketReceiveEvent::class.java)
                || InternalEventBus.isListening(PacketReceiveEvent.Handshake::class.java)
                || InternalEventBus.isListening(PacketSendEvent::class.java)
                || InternalEventBus.isListening(PacketSendEvent.Handshake::class.java)
    }

    /**
     * 立刻向服务端注入数据包监听器
     * 并且更新 OpenContainer 缓存
     */
    private fun injectPacketListener() {
        instance = LightInjectorImpl(BukkitPlugin.getInstance())
        Exchanges[PACKET_LISTENER] = pluginId
        Exchanges["$PACKET_LISTENER/plugin/$pluginId"] = null
        updateContainer()
        debug("LightInjector 初始化完成。")
    }

    /**
     * 初始化 LightInjector
     */
    @Awake(LifeCycle.ENABLE)
    private fun onEnable() {
        if (TabooLib.isStopped()) {
            return
        }
        // 所有 TabooLib 6.2 只注入一个数据包监听器
        // 借助 Exchanges 和 OpenAPI 减少资源损耗
        if (Exchanges.contains(PACKET_LISTENER)) {
            // 是否监听了数据包相关事件
            // 只有在 Exchanges 中标记本插件，才会收到共享的数据包事件
            if (isPacketEventListened()) {
                Exchanges["$PACKET_LISTENER/plugin/$pluginId"] = true
                debug("LightInjector 已在其他插件中初始化。")
            }
        } else {
            injectPacketListener()
        }
    }

    /**
     * 卸载 LightInjector，需要其他插件立刻顶替
     */
    @Awake(LifeCycle.DISABLE)
    private fun onDisable() {
        if (TabooLib.isStopped() || !isBukkitServerRunning) {
            return
        }
        if (instance != null) {
            // 注销数据包监听器
            instance?.close()
            // 通知其他插件立刻接管
            val next = containers.firstOrNull { it.name != pluginId }
            if (next != null) {
                try {
                    if (next.call(PACKET_LISTENER_EJECT, arrayOf()).isSuccessful) {
                        debug("LightInjector closed, current packet listener is taken over by ${next.name}.")
                    }
                } catch (ex: IllegalStateException) {
                    if (ex.message != "zip file closed") ex.printStackTrace()
                }
            }
        }
    }

    @Awake(LifeCycle.ACTIVE)
    private fun onActive() {
        if (instance != null) {
            updateContainer()
        }
    }

    @SubscribeEvent
    private fun onEnabled(e: PluginEnableEvent) {
        if (instance != null) {
            updateContainer()
        }
    }

    @SubscribeEvent
    private fun onDisable(e: PluginDisableEvent) {
        if (instance != null) {
            updateContainer()
        }
    }

    override fun call(name: String, data: Array<Any?>): OpenResult {
        when (name) {
            // 数据包接收
            PACKET_RECEIVE -> {
                val player = data[0] as? Player
                val channel = data[1] as Channel
                val packet = data[2] as Any
                if (player != null) {
                    val event = PacketReceiveEvent(player, PacketImpl(packet))
                    return if (event.callIf()) OpenResult.successful(event.packet.source) else OpenResult.failed()
                } else {
                    val event = PacketReceiveEvent.Handshake(channel, PacketImpl(packet))
                    return if (event.callIf()) OpenResult.successful(event.packet.source) else OpenResult.failed()
                }
            }
            // 数据包发送
            PACKET_SEND -> {
                val player = data[0] as? Player
                val channel = data[1] as Channel
                val packet = data[2] as Any
                if (player != null) {
                    val event = PacketSendEvent(player, PacketImpl(packet))
                    return if (event.callIf()) OpenResult.successful(event.packet.source) else OpenResult.failed()
                } else {
                    val event = PacketSendEvent.Handshake(channel, PacketImpl(packet))
                    return if (event.callIf()) OpenResult.successful(event.packet.source) else OpenResult.failed()
                }
            }
            // 数据包监听器注销
            PACKET_LISTENER_EJECT -> {
                try {
                    injectPacketListener()
                    return OpenResult.successful()
                } catch (ex: Throwable) {
                    if (isDebugMode) ex.printStackTrace()
                }
            }
        }
        return OpenResult.failed()
    }
}
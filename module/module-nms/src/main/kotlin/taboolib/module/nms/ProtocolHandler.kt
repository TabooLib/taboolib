package taboolib.module.nms

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.dev
import taboolib.common.platform.function.isListened
import taboolib.platform.BukkitPlugin

/**
 * @author 坏黑
 * @since 2018-10-28 14:34
 */
@Inject
@PlatformSide(Platform.BUKKIT)
object ProtocolHandler {

    private var isDisabled = false

    /**
     * 使用 LightInjector 作为 TabooLib 数据包实现。
     * 不再对外开放，因为随着版本更新，底层实现可能会变更，在这之前曾使用 TinyProtocol。
     *
     * 这类工具现均已停止维护，可能因服务端改动频繁维护成本极高。
     * 未来可能会选择使用 retrooper/packetevents，但它是个巨无霸。
     */
    private var instance: LightInjector? = null

    /**
     * 禁用 Channel 注入
     */
    fun disable() {
        isDisabled = true
    }

    /**
     * 数据包事件是否被当前插件监听
     */
    fun isPacketEventListened(): Boolean {
        return PacketSendEvent::class.java.isListened() || PacketReceiveEvent::class.java.isListened()
    }

    @Awake(LifeCycle.ENABLE)
    private fun onEnable() {
        if (TabooLib.isStopped() || isDisabled || !isPacketEventListened()) {
            return
        }
        instance = LightInjectorImpl(BukkitPlugin.getInstance())
        dev("LightInjector initialized.")
    }

    @Awake(LifeCycle.DISABLE)
    private fun onDisable() {
        if (TabooLib.isStopped()) {
            return
        }
        instance?.close()
    }
}
package taboolib.module.nms

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.isListened

/**
 * @author 坏黑
 * @since 2018-10-28 14:34
 */
@Inject
@PlatformSide(Platform.BUKKIT)
object ProtocolHandler {

    private var isDisabled = false

    /** TinyProtocol 实例 */
    var instance: TinyProtocol? = null
        private set

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
        instance = TinyProtocol()
    }

    @Awake(LifeCycle.DISABLE)
    private fun onDisable() {
        if (TabooLib.isStopped()) {
            return
        }
        instance?.close()
    }
}
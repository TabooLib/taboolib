package taboolib.module.nms.test

import org.bukkit.Bukkit
import org.tabooproject.reflex.Reflex.Companion.invokeConstructor
import org.tabooproject.reflex.Reflex.Companion.unsafeInstance
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.Test
import taboolib.common.event.InternalEventBus
import taboolib.common.io.isDevelopmentMode
import taboolib.common.platform.Awake
import taboolib.module.nms.*

/**
 * TabooLib
 * taboolib.module.nms.test.TestPacketSender
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
@Inject
object TestNMSPacket : Test() {

    var testSend = false
    var testSendHandshake = false
    var testReceive = false
    var testReceiveHandshake = false

    @Awake(LifeCycle.LOAD)
    fun setup() {
        if (isDevelopmentMode) {
            InternalEventBus.listen(PacketSendEvent::class.java) { testSend = true }
            InternalEventBus.listen(PacketSendEvent.Handshake::class.java) { testSendHandshake = true }
            InternalEventBus.listen(PacketReceiveEvent::class.java) { testReceive = true }
            InternalEventBus.listen(PacketReceiveEvent.Handshake::class.java) { testReceiveHandshake = true }
        }
    }

    override fun check(): List<Result> {
        val result = arrayListOf<Result>()
        val player = Bukkit.getOnlinePlayers().firstOrNull()
        if (player != null) {
            // 测试连接
            result += sandbox("NMS:getConnection(Player)") { PacketSender.getConnection(player) }
            // 测试发包
            result += sandbox("NMS:sendPacketBlocking(Player, Any)") {
                try {
                    player.sendPacketBlocking(nmsClass("PacketPlayOutKeepAlive").unsafeInstance())
                } catch (ex: ClassNotFoundException) {
                    player.sendPacketBlocking(nmsClass("PacketPlayOutViewDistance").invokeConstructor(8))
                }
            }
            result += sandbox("NMS:sendBundlePacketBlocking(Player, Any)") {
                try {
                    player.sendBundlePacketBlocking(nmsClass("PacketPlayOutKeepAlive").unsafeInstance())
                } catch (ex: ClassNotFoundException) {
                    player.sendBundlePacketBlocking(nmsClass("PacketPlayOutViewDistance").invokeConstructor(8))
                }
            }
            // 测试事件
            result += if (testSend) Success.of("NMS:PacketSendEvent") else Failure.of("NMS:PacketSendEvent", "NOT_TRIGGERED")
            result += if (testSendHandshake) Success.of("NMS:PacketSendEvent.Handshake") else Failure.of("NMS:PacketSendEvent.Handshake", "NOT_TRIGGERED")
            result += if (testReceive) Success.of("NMS:PacketReceiveEvent") else Failure.of("NMS:PacketReceiveEvent", "NOT_TRIGGERED")
            result += if (testReceiveHandshake) Success.of("NMS:PacketReceiveEvent.Handshake") else Failure.of("NMS:PacketReceiveEvent.Handshake", "NOT_TRIGGERED")
        }
        return result
    }
}
package taboolib.test.nms

import org.tabooproject.reflex.Reflex.Companion.unsafeInstance
import taboolib.common.Isolated
import taboolib.common.Test
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.*
import taboolib.platform.util.onlinePlayers

/**
 * TabooLib
 * taboolib.module.nms.test.TestPacketSender
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
@Isolated
object TestPacketSender : Test() {

    var testSend = false
    var testReceive = false

    override fun check(): List<Result> {
        val result = arrayListOf<Result>()
        result += sandbox("NMS:getConnections()") { nmsProxy<ConnectionGetter>().getConnections() }
        val player = onlinePlayers.firstOrNull()
        if (player != null) {
            result += sandbox("NMS:getConnection(Player)") { PacketSender.getConnection(player) }
            result += sandbox("NMS:sendPacketBlocking(Player, Any)") { player.sendPacketBlocking(nmsClass("PacketPlayOutKeepAlive").unsafeInstance()) }
            result += sandbox("NMS:sendBundlePacketBlocking(Player, Any)") { player.sendBundlePacketBlocking(nmsClass("PacketPlayOutKeepAlive").unsafeInstance()) }
            result += if (testSend) Success.of("NMS:PacketSendEvent") else Failure.of("NMS:PacketSendEvent", "NOT_TRIGGERED")
            result += if (testReceive) Success.of("NMS:PacketReceiveEvent") else Failure.of("NMS:PacketReceiveEvent", "NOT_TRIGGERED")
        }
        return result
    }

    @SubscribeEvent
    private fun onSend(e: PacketSendEvent) {
        testSend = true
    }

    @SubscribeEvent
    private fun onReceive(e: PacketReceiveEvent) {
        testReceive = true
    }
}
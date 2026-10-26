package taboolib.module.nms

import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.Inject
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.BukkitPlugin
import taboolib.platform.Folia
import taboolib.platform.FoliaExecutor
import java.util.concurrent.ConcurrentHashMap

// region NMSSignListener
/**
 * 接收牌子更新包并完成输入回调。
 *
 * @author sky
 */
@Inject
@PlatformSide(Platform.BUKKIT)
internal object NMSSignListener {

    /**
     * 等待用户输入的回调。
     */
    val callback = ConcurrentHashMap<String, (Array<String>) -> Unit>()

    /**
     * 玩家离开时清理尚未完成的输入回调。
     *
     * @param event 玩家离开事件
     */
    @SubscribeEvent
    fun onQuit(event: PlayerQuitEvent) {
        callback.remove(event.player.name)
    }

    /**
     * 处理玩家提交的牌子文本，每个回调只会消费一次。
     *
     * @param event 数据包接收事件
     */
    @SubscribeEvent
    fun onReceive(event: PacketReceiveEvent) {
        if ((event.packet.name == "PacketPlayInUpdateSign" || event.packet.name == "ServerboundSignUpdatePacket") && callback.containsKey(event.player.name)) {
            val function = callback.remove(event.player.name) ?: return
            val lines = when {
                MinecraftVersion.isHigherOrEqual(MinecraftVersion.V26_3) -> event.packet.read<List<String>>("lines")!!.toTypedArray()
                MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_17) -> event.packet.read<Array<String>>("lines")!!
                MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_9) -> event.packet.read<Array<String>>("b")!!
                else -> event.packet.read<Array<Any>>("b")!!.map { NMSSign.instance.deserialize(it) }.toTypedArray()
            }
            if (Folia.isFolia) {
                FoliaExecutor.REGION_SCHEDULER.run(BukkitPlugin.getInstance(), event.player.location) {
                    function.invoke(lines)
                }
            } else {
                submit { function.invoke(lines) }
            }
        }
    }
}
// endregion

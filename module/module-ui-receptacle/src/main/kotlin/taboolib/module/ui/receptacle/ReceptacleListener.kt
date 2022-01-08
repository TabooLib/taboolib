package taboolib.module.ui.receptacle

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.PacketReceiveEvent
import taboolib.module.ui.receptacle.operates.OperateWindowClose
import taboolib.module.ui.receptacle.operates.OperateWindowSetSlot

@PlatformSide([Platform.BUKKIT])
object ReceptacleListener {

    @SubscribeEvent
    fun onPacket(e: PacketReceiveEvent) {
        val receptacle = e.player.getViewingReceptacle() ?: return
        if (e.packet.name == "PacketPlayInWindowClick") {
            val id = if (MinecraftVersion.isUniversal) {
                e.packet.read<Int>("containerId")
            } else {
                e.packet.read<Int>("a")
            }
            if (id == 119) {
                val slot: Int
                val clickType: ReceptacleClickType
                val button: Int
                if (MinecraftVersion.isUniversal) {
                    slot = e.packet.read<Int>("slotNum")!!
                    button = e.packet.read<Int>("buttonNum")!!
                    clickType = ReceptacleClickType.from(e.packet.read<Any>("clickType").toString(), button, slot) ?: return
                } else if (MinecraftVersion.majorLegacy >= 10900) {
                    slot = e.packet.read<Int>("slot")!!
                    button = e.packet.read<Int>("button")!!
                    clickType = ReceptacleClickType.from(e.packet.read<Any>("shift").toString(), button, slot) ?: return
                } else {
                    slot = e.packet.read<Int>("slot")!!
                    button = e.packet.read<Int>("button")!!
                    clickType = ReceptacleClickType.from(e.packet.read<Int>("shift")!!, button, slot) ?: return
                }
                val evt = ReceptacleInteractEvent(e.player, receptacle, clickType, slot)
                evt.call()
                receptacle.callEventClick(evt)
                if (evt.isCancelled) {
                    OperateWindowSetSlot(slot = -1, windowId = -1, packet = true).send(e.player)
                }
                e.isCancelled = true
            }
        } else if (e.packet.name == "PacketPlayInCloseWindow") {
            val id = if (MinecraftVersion.isUniversal) {
                e.packet.read<Int>("containerId")
            } else {
                e.packet.read<Int>("id")
            }
            if (id == 119) {
                receptacle.close(false)
                // 防止关闭菜单后, 动态标题频率过快出现的卡假容器
                submit(delay = 1, async = true) {
                    val viewingReceptacle = e.player.getViewingReceptacle()
                    if (viewingReceptacle != null) {
                        e.player.updateInventory()
                    }
                }
                submit(delay = 4, async = true) {
                    val viewingReceptacle = e.player.getViewingReceptacle()
                    if (viewingReceptacle == receptacle) {
                        OperateWindowClose(packet = receptacle.packet).send(e.player)
                    }
                }
            }
            e.isCancelled = true
            ReceptacleCloseEvent(e.player, receptacle).call()
        }
    }
}
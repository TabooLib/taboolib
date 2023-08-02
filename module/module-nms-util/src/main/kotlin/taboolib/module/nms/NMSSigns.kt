@file:Isolated
@file:Suppress("DEPRECATION")

package taboolib.module.nms

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.Isolated
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common.util.unsafeLazy
import taboolib.library.xseries.XMaterial
import java.util.concurrent.ConcurrentHashMap

fun Player.inputSign(lines: Array<String> = arrayOf(), function: (lines: Array<String>) -> Unit) {
    val location = location.clone()
    // 如果版本低于 1.20，则修改 y 到 0
    if (MinecraftVersion.major < 12) {
        location.y = 0.0
    } else {
        // 否则 y - 2
        location.y -= 2
    }
    // 发送虚拟牌子
    try {
        sendBlockChange(location, XMaterial.OAK_WALL_SIGN.parseMaterial()!!.createBlockData())
    } catch (t: NoSuchMethodError) {
        sendBlockChange(location, XMaterial.OAK_WALL_SIGN.parseMaterial()!!, 0.toByte())
    }
    // 设置牌子内容
    try {
        sendSignChange(location, lines.formatSign(4))
    } catch (ex: Throwable) {
        sendSignChange(location, lines.formatSign(3))
    }
    // 注册回调函数
    SignsListener.inputs[name] = {
        function(it)
        // 回收牌子
        try {
            sendBlockChange(location, location.block.blockData)
        } catch (t: NoSuchMethodError) {
            sendBlockChange(location, location.block.type, location.block.data)
        }
    }
    // 使玩家打开牌子
    nmsGeneric.openSignEditor(this, location.block)
}

private fun Array<String>.formatSign(line: Int): Array<String> {
    val list = toMutableList()
    while (list.size < line) {
        list.add("")
    }
    while (list.size > line) {
        list.removeLast()
    }
    return list.toTypedArray()
}

@Isolated
@PlatformSide([Platform.BUKKIT])
internal object SignsListener {

    val inputs = ConcurrentHashMap<String, (Array<String>) -> Unit>()

    val classChatSerializer by unsafeLazy { nmsClass("IChatBaseComponent\$ChatSerializer") }

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        inputs.remove(e.player.name)
    }

    @SubscribeEvent
    fun onReceive(e: PacketReceiveEvent) {
        if (e.packet.name == "PacketPlayInUpdateSign" && inputs.containsKey(e.player.name)) {
            val function = inputs.remove(e.player.name) ?: return
            val lines = when {
                MinecraftVersion.majorLegacy > 11700 -> e.packet.read<Array<String>>("lines")!!
                MinecraftVersion.majorLegacy > 10900 -> e.packet.read<Array<String>>("b")!!
                else -> {
                    e.packet.read<Array<Any>>("b")!!.map { classChatSerializer.invokeMethod<String>("a", it, isStatic = true)!! }.toTypedArray()
                }
            }
            submit { function.invoke(lines) }
        }
    }
}
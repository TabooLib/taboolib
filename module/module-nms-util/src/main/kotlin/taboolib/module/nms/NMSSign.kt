package taboolib.module.nms

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common.util.unsafeLazy
import taboolib.library.xseries.XMaterial
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap
import kotlin.Any
import kotlin.Array
import kotlin.Int
import kotlin.String
import kotlin.Throwable
import kotlin.Unit
import kotlin.arrayOf

/**
 * 捕获玩家的牌子输入
 *
 * @param lines 牌子内容（不足 4 行补齐至 4 行）
 * @param callback 回调函数
 */
fun Player.inputSign(lines: Array<String> = arrayOf(), callback: (lines: Array<String>) -> Unit) {
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
    NMSSignListener.callback[name] = {
        callback(it)
        // 回收牌子
        try {
            sendBlockChange(location, location.block.blockData)
        } catch (t: NoSuchMethodError) {
            sendBlockChange(location, location.block.type, location.block.data)
        }
    }
    nmsProxy<NMSSign>().openSignEditor(this, location.block)
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

/**
 * TabooLib
 * taboolib.module.nms.NMSSign
 *
 * @author 坏黑
 * @since 2023/5/2 21:57
 */
abstract class NMSSign {

    abstract fun deserialize(component: Any): String

    abstract fun openSignEditor(player: Player, block: Block)
}

/**
 * [NMSSign] 的实现类
 */
class NMSSignImpl : NMSSign() {

    val constructorPacketOutSignEditor: Constructor<*> by unsafeLazy {
        net.minecraft.server.v1_16_R1.PacketPlayOutOpenSignEditor::class.java.getDeclaredConstructor(
            net.minecraft.server.v1_16_R1.BlockPosition::class.java,
            java.lang.Boolean.TYPE
        )
    }

    override fun deserialize(component: Any): String {
        return net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer.a(component as net.minecraft.server.v1_12_R1.IChatBaseComponent)
    }

    override fun openSignEditor(player: Player, block: Block) {
        try {
            val blockPosition = net.minecraft.server.v1_12_R1.BlockPosition(block.x, block.y, block.z)
            // 1.20 -> 正反牌子
            if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_20)) {
                player.sendPacket(constructorPacketOutSignEditor.newInstance(blockPosition, true))
            } else {
                player.sendPacket(net.minecraft.server.v1_12_R1.PacketPlayOutOpenSignEditor(blockPosition))
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }
}

@PlatformSide([Platform.BUKKIT])
private object NMSSignListener {

    /** 用户输入 */
    val callback = ConcurrentHashMap<String, (Array<String>) -> Unit>()

    @SubscribeEvent
    fun onQuit(e: PlayerQuitEvent) {
        callback.remove(e.player.name)
    }

    @SubscribeEvent
    fun onReceive(e: PacketReceiveEvent) {
        if (e.packet.name == "PacketPlayInUpdateSign" && callback.containsKey(e.player.name)) {
            val function = callback.remove(e.player.name) ?: return
            val lines = when {
                MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_17) -> e.packet.read<Array<String>>("lines")!!
                MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_9) -> e.packet.read<Array<String>>("b")!!
                else -> e.packet.read<Array<Any>>("b")!!.map { nmsProxy<NMSSign>().deserialize(it) }.toTypedArray()
            }
            submit { function.invoke(lines) }
        }
    }
}
package taboolib.module.nms

import net.minecraft.network.protocol.game.ClientboundBundlePacket

/** 是否为 BundlePacket */
fun Packet.isBundlePacket(): Boolean {
    return name == "ClientboundBundlePacket"
}

/** 获取 BundlePacket 的子数据包 */
fun Packet.subPackets(): List<Packet> {
    return if (isBundlePacket()) nmsProxy<NMSBundlePacket>().readPackets(source).map { PacketImpl(it) } else arrayListOf()
}

/** 设置 BundlePacket 的子数据包 */
fun List<Packet>.createBundlePacket(): Packet {
    return PacketImpl(PacketSender.createBundlePacket(map { it.source }) ?: error("Cannot create bundle packet."))
}

/**
 * TabooLib
 * taboolib.module.nms.NMSBundlePacket
 *
 * @author 坏黑
 * @since 2024/5/6 17:31
 */
abstract class NMSBundlePacket {

    abstract fun readPackets(packet: Any): MutableIterable<Any>
}

class NMSBundlePacketImpl : NMSBundlePacket() {

    override fun readPackets(packet: Any): MutableIterable<Any> {
        return (packet as ClientboundBundlePacket).subPackets()
    }
}
package taboolib.module.nms

import io.netty.channel.Channel
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 * TabooLib
 * taboolib.module.nms.ConnectionGetter
 *
 * @author 坏黑
 * @since 2023/1/31 20:50
 */
abstract class ConnectionGetter {

    /**
     * 获取服务器中的所有连接
     */
    abstract fun getConnections(): List<Any>

    /**
     * 根据地址获取连接
     * @param isFirst 是否首次获取（将占用连接）
     */
    abstract fun getConnection(address: InetAddress, isFirst: Boolean): Any

    /**
     * 获取连接的 [InetSocketAddress]
     */
    abstract fun getAddress(connection: Any): InetSocketAddress

    /**
     * 获取连接对应的 [Channel]
     */
    abstract fun getChannel(connection: Any): Channel

    /**
     * 获取地址对应的 [Channel]
     */
    abstract fun getChannel(address: InetAddress, isFirst: Boolean): Channel

    /**
     * 释放被该地址占用的对应的连接
     */
    abstract fun release(address: InetSocketAddress)

    /**
     * 创建一个 BundlePacket
     */
    abstract fun newBundlePacket(packets: List<Any>): Any
}
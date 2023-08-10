package taboolib.module.nms

import io.netty.channel.Channel
import net.minecraft.network.NetworkManager
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.PacketListenerPlayOut
import net.minecraft.server.network.ServerConnection
import org.bukkit.Bukkit
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.UnsupportedVersionException
import taboolib.common.io.isDevelopmentMode
import taboolib.common.platform.function.dev
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.ConnectionGetterImpl
 *
 * @author 坏黑
 * @since 2023/1/31 20:50
 */
@Suppress("UNCHECKED_CAST")
class ConnectionGetterImpl : ConnectionGetter() {

    val major = MinecraftVersion.major
    val addressUsed = ConcurrentHashMap<InetSocketAddress, Any>()

    override fun getConnections(): List<Any> {
        return when (major) {
            // 1.8, 1.9, 1.10, 1.11, 1.12 -> List<NetworkManager> h
            in MinecraftVersion.V1_8..MinecraftVersion.V1_12 -> {
                ((Bukkit.getServer() as CraftServer8).server as NMSMinecraftServer8).serverConnection.getProperty<List<Any>>("h")!!
            }
            // 1.13 -> List<NetworkManager> g
            MinecraftVersion.V1_13 -> {
                ((Bukkit.getServer() as CraftServer8).server as NMSMinecraftServer8).serverConnection.getProperty<List<Any>>("g")!!
            }
            // 1.14 -> List<NetworkManager> g
            // java.lang.NoSuchMethodError: 'net.minecraft.server.v1_16_R3.MinecraftServer org.bukkit.craftbukkit.v1_16_R3.CraftServer.getServer()'
            MinecraftVersion.V1_14 -> {
                ((Bukkit.getServer() as CraftServer16).server as NMSMinecraftServer16).serverConnection?.getProperty<List<Any>>("g")!!
            }
            // 1.15, 1.16 -> List<NetworkManager> connectedChannels
            MinecraftVersion.V1_15, MinecraftVersion.V1_16 -> {
                ((Bukkit.getServer() as CraftServer16).server as NMSMinecraftServer16).serverConnection?.getProperty<List<Any>>("connectedChannels")!!
            }
            // 1.17 -> List<NetworkManager> getConnections()
            // 傻逼项目引入依赖天天出问题，滚去反射吧
            MinecraftVersion.V1_17 -> {
                ((Bukkit.getServer() as CraftServer19).server as NMSMinecraftServer).invokeMethod<ServerConnection>("getServerConnection")!!.connections
            }
            // 1.18, 1.19, 1.20 -> List<NetworkManager> getConnections()
            // 这个版本开始获取 ServerConnection 的方法变更为 getConnection()
            in MinecraftVersion.V1_18..MinecraftVersion.V1_20 -> {
                ((Bukkit.getServer() as CraftServer19).server as NMSMinecraftServer).connection?.connections ?: error("Unable to get connections from ${Bukkit.getServer()}")
            }
            // 不支持
            else -> throw UnsupportedVersionException()
        }
    }

    override fun getConnection(address: InetAddress, isFirst: Boolean): Any {
        // 获取服务器中的所有连接
        val serverConnections = getConnections()
        // 获取相同 IP 的连接
        val connections = serverConnections.filter { conn -> conn.address().address == address }
        // 没有相同 IP 的连接
        if (connections.isEmpty()) {
            warning("No connection found with the same address (${address})")
            warning("Server connections:")
            serverConnections.forEach { conn -> warning("- ${conn.address()}") }
            throw IllegalStateException()
        }
        // 打印信息
        if (isDevelopmentMode) {
            info("Player connection ($address)")
            info("Server connections:")
            serverConnections.forEach { conn -> info("- ${conn.address()}") }
        }
        // 是否进行初始化
        val connection = if (isFirst) {
            // 获取未被使用的连接
            val unused = connections.find { conn -> !addressUsed.containsKey(conn.address()) }
            if (unused == null) {
                warning("Connections with the same address are already occupied (${address})")
                warning("Server connections:")
                serverConnections.forEach { conn -> warning("- ${conn.address()}") }
                throw IllegalStateException()
            }
            addressUsed[unused.address()] = unused
            unused
        } else {
            // 获取已使用的连接
            val used = connections.find { conn -> addressUsed[conn.address()] == conn }
            // 没有找到玩家之前存入插件的连接
            if (used == null) {
                warning("Get the connection before initialisation (${address})")
                warning("Server connections:")
                serverConnections.forEach { conn -> warning("- ${conn.address()}") }
                throw IllegalStateException()
            }
            used
        }
        dev("Player connection ($address) -> ${connection.address()} (${if (isFirst) "init" else "get"})")
        return connection
    }

    override fun getAddress(connection: Any): InetSocketAddress {
        // 这种方式无法在 BungeeCord 中获取到正确的地址：
        // return (getChannel(connection).remoteAddress() as? InetSocketAddress)?.address
        // 因此要根据不同的版本获取不同的 SocketAddress 字段：
        return when (major) {
            // 1.8, 1.9, 1.10, 1.11, 1.12
            // public SocketAddress l;
            in MinecraftVersion.V1_8..MinecraftVersion.V1_12 -> ((connection as NMSNetworkManager8).l as InetSocketAddress)
            // 1.13, 1.14, 1.15, 1.16
            // public SocketAddress socketAddress;
            in MinecraftVersion.V1_13..MinecraftVersion.V1_16 -> ((connection as NMSNetworkManager13).socketAddress as InetSocketAddress)
            // 1.17, 1.18, 1.19, 1.20
            // public SocketAddress address;
            in MinecraftVersion.V1_17..MinecraftVersion.V1_20 -> ((connection as NetworkManager).address as InetSocketAddress)
            // 不支持
            else -> throw UnsupportedVersionException()
        }
    }

    override fun getChannel(connection: Any): Channel {
        return (connection as NMSNetworkManager8).channel
    }

    override fun getChannel(address: InetAddress, isFirst: Boolean): Channel {
        return getChannel(getConnection(address, isFirst))
    }

    override fun release(address: InetSocketAddress) {
        addressUsed.remove(address)
    }

    override fun newBundlePacket(packets: List<Any>): Any {
        return ClientboundBundlePacket(packets.asIterable() as Iterable<Packet<PacketListenerPlayOut>>)
    }

    private fun Any.address(): InetSocketAddress {
        return getAddress(this)
    }
}

private typealias CraftServer8 = org.bukkit.craftbukkit.v1_8_R3.CraftServer

private typealias CraftServer16 = org.bukkit.craftbukkit.v1_16_R2.CraftServer

private typealias CraftServer19 = org.bukkit.craftbukkit.v1_19_R3.CraftServer

private typealias NMSMinecraftServer8 = net.minecraft.server.v1_8_R3.MinecraftServer

private typealias NMSMinecraftServer16 = net.minecraft.server.v1_16_R2.MinecraftServer

private typealias NMSMinecraftServer = net.minecraft.server.MinecraftServer

private typealias NMSNetworkManager8 = net.minecraft.server.v1_8_R3.NetworkManager

private typealias NMSNetworkManager13 = net.minecraft.server.v1_13_R2.NetworkManager
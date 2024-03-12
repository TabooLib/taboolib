package taboolib.module.nms

import io.netty.channel.Channel
import net.minecraft.network.NetworkManager
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.PacketListenerPlayOut
import net.minecraft.server.network.ServerConnection
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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

    class NMSConnection(val source: Any): MinecraftConnection

    val major = MinecraftVersion.major
    val addressUsed = ConcurrentHashMap<InetSocketAddress, MinecraftConnection>()

    override fun getConnections(): List<MinecraftConnection> {
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
                ((Bukkit.getServer() as CraftServer19).server as NMSMinecraftServer).invokeMethod<ServerConnection>("getServerConnection")!!.getProperty<List<Any>>("g")!!
            }
            // 1.18, 1.19, 1.20 -> List<NetworkManager> getConnections()
            // 这个版本开始获取 ServerConnection 的方法变更为 getConnection()
            in MinecraftVersion.V1_18..MinecraftVersion.V1_20 -> {
                ((Bukkit.getServer() as CraftServer19).server as NMSMinecraftServer).connection?.connections ?: error("Unable to get connections from ${Bukkit.getServer()}")
            }
            // 不支持
            else -> throw UnsupportedVersionException()
        }.map { NMSConnection(it) }
    }

    /**
     * 2024/03/12 遇到了一个逆天问题, 1.12 paper
     * 玩家进入服务器时，不知道为什么会连出多个 connection，导致无法获取到正确的 channel，发包系统报废。
     * ```
     * [23:04:11] [User Authenticator #1/INFO]: UUID of player ****** is ********-****-****-****-************
     * [23:04:11] [Server thread/INFO]: [Adyeshach] Player connection (/125.115.*.*) <-- 在 PlayerLoginEvent 触发时，无法获取到玩家的端口号
     * [23:04:11] [Server thread/INFO]: [Adyeshach] Server connections:
     * [23:04:11] [Server thread/INFO]: [Adyeshach] - /111.50.*.*:5742  <-- 服务器里根本没有这个玩家
     * [23:04:11] [Server thread/INFO]: [Adyeshach] - /125.115.*.*:9013
     * [23:04:11] [Server thread/INFO]: [Adyeshach] - /125.115.*.*:9014
     * [23:04:11] [Server thread/INFO]: [Adyeshach] - /125.115.*.*:9023 <-- 重复的玩家连接
     * ```
     * 这些重复的连接里，open=true, active=true, connected=true
     * ...
     * 诶，真是傻逼。
     */
    override fun getConnection(address: InetAddress, isFirst: Boolean): List<MinecraftConnection> {
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
        val findList = if (isFirst) {
            // 获取未被使用的连接
            val unused = connections.filter { conn -> !addressUsed.containsKey(conn.address()) }
            if (unused.isEmpty()) {
                warning("Connections with the same address are already occupied (${address})")
                warning("Server connections:")
                serverConnections.forEach { conn -> warning("- ${conn.address()}") }
                throw IllegalStateException()
            }
            // 如果有多个未被占用的相同 IP 的链接
            if (unused.size > 1) {
                warning("Multiple connections with the same address (${address})")
                warning("Unused connections:")
                unused.forEach { conn -> warning("- ${conn.address()}") }
            }
            unused.forEach { conn -> addressUsed[conn.address()] = conn }
            unused
        } else {
            // 获取已使用的连接
            val used = connections.filter { conn -> addressUsed[conn.address()] == conn }
            // 没有找到玩家之前存入插件的连接
            if (used.isEmpty()) {
                warning("Get the connection before initialisation (${address})")
                warning("Server connections:")
                serverConnections.forEach { conn -> warning("- ${conn.address()}") }
                throw IllegalStateException()
            }
            used
        }
        findList.forEach { find -> dev("Player connection ($address) -> ${find.address()} (${if (isFirst) "init" else "get"})") }
        return findList
    }

    override fun getAddress(connection: MinecraftConnection): InetSocketAddress {
        connection as NMSConnection
        // 这种方式无法在 BungeeCord 中获取到正确的地址：
        // return (getChannel(connection).remoteAddress() as? InetSocketAddress)?.address
        // 因此要根据不同的版本获取不同的 SocketAddress 字段：
        return when (major) {
            // 1.8, 1.9, 1.10, 1.11, 1.12
            // public SocketAddress l;
            in MinecraftVersion.V1_8..MinecraftVersion.V1_12 -> ((connection.source as NMSNetworkManager8).l as InetSocketAddress)
            // 1.13, 1.14, 1.15, 1.16
            // public SocketAddress socketAddress;
            in MinecraftVersion.V1_13..MinecraftVersion.V1_16 -> ((connection.source as NMSNetworkManager13).socketAddress as InetSocketAddress)
            // 1.17, 1.18, 1.19, 1.20
            // public SocketAddress address;
            in MinecraftVersion.V1_17..MinecraftVersion.V1_20 -> ((connection.source as NetworkManager).address as InetSocketAddress)
            // 不支持
            else -> throw UnsupportedVersionException()
        }
    }

    override fun getChannel(connection: MinecraftConnection): Channel {
        return ((connection as NMSConnection).source as NMSNetworkManager8).channel
    }

    override fun getChannel(player: Player, address: InetAddress, isFirst: Boolean): List<Channel> {
        return getConnection(address, isFirst).map { getChannel(it) }
    }

    override fun release(address: InetSocketAddress) {
        addressUsed.remove(address)
    }

    override fun newBundlePacket(packets: List<Any>): Any {
        return ClientboundBundlePacket(packets.asIterable() as Iterable<Packet<PacketListenerPlayOut>>)
    }
}

private typealias CraftServer8 = org.bukkit.craftbukkit.v1_8_R3.CraftServer

private typealias CraftServer16 = org.bukkit.craftbukkit.v1_16_R3.CraftServer

private typealias CraftServer19 = org.bukkit.craftbukkit.v1_19_R3.CraftServer

private typealias NMSMinecraftServer8 = net.minecraft.server.v1_8_R3.MinecraftServer

private typealias NMSMinecraftServer16 = net.minecraft.server.v1_16_R3.MinecraftServer

private typealias NMSMinecraftServer = net.minecraft.server.MinecraftServer

private typealias NMSNetworkManager8 = net.minecraft.server.v1_8_R3.NetworkManager

private typealias NMSNetworkManager13 = net.minecraft.server.v1_13_R2.NetworkManager

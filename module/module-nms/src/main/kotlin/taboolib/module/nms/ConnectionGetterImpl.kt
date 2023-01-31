package taboolib.module.nms

import io.netty.channel.Channel
import net.minecraft.server.network.ServerConnection
import org.bukkit.Bukkit
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 * TabooLib
 * taboolib.module.nms.ConnectionGetterImpl
 *
 * @author 坏黑
 * @since 2023/1/31 20:50
 */
class ConnectionGetterImpl : ConnectionGetter() {

    val major = MinecraftVersion.major

    override fun getConnection(address: InetAddress): Any? {
        val connections = when (major) {
            // 1.8, 1.9, 1.10, 1.11, 1.12 -> List<NetworkManager> h
            0, 1, 2, 3, 4 -> {
                ((Bukkit.getServer() as CraftServer8).server as NMS8MinecraftServer).serverConnection.getProperty<List<Any>>("h")
            }
            // 1.13, 1.14 -> List<NetworkManager> g
            5, 6 -> {
                ((Bukkit.getServer() as CraftServer8).server as NMS8MinecraftServer).serverConnection.getProperty<List<Any>>("g")
            }
            // 1.15, 1.16 -> List<NetworkManager> connectedChannels
            7, 8 -> {
                ((Bukkit.getServer() as CraftServer8).server as NMS8MinecraftServer).serverConnection.getProperty<List<Any>>("connectedChannels")
            }
            // 1.17 -> List<NetworkManager> getConnections()
            // 傻逼项目引入依赖天天出问题，滚去反射吧
            9 -> {
                ((Bukkit.getServer() as CraftServer19).server as NMSMinecraftServer).invokeMethod<ServerConnection>("getServerConnection")?.connections
            }
            // 1.18, 1.19 -> List<NetworkManager> getConnections()
            // 这个版本开始获取 ServerConnection 的方法变更为 getConnection()
            10, 11 -> {
                ((Bukkit.getServer() as CraftServer19).server as NMSMinecraftServer).connection?.connections
            }
            // 不支持
            else -> error("Unsupported Minecraft version: $major")
        } ?: error("Unable to get connections from ${Bukkit.getServer()}")
        return connections.firstOrNull { getAddress(it) == address }
    }

    override fun getChannel(connection: Any): Channel {
        return (connection as NMS8NetworkManager).channel
    }

    private fun getAddress(connection: Any): InetAddress? {
        return (getChannel(connection).remoteAddress() as? InetSocketAddress)?.address
    }
}

typealias CraftServer8 = org.bukkit.craftbukkit.v1_8_R3.CraftServer

typealias CraftServer19 = org.bukkit.craftbukkit.v1_19_R2.CraftServer

typealias NMS8MinecraftServer = net.minecraft.server.v1_8_R3.MinecraftServer

typealias NMSMinecraftServer = net.minecraft.server.MinecraftServer

typealias NMS8NetworkManager = net.minecraft.server.v1_8_R3.NetworkManager
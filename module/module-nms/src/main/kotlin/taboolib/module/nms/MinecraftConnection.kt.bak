package taboolib.module.nms

import java.net.InetSocketAddress

/**
 * TabooLib
 * taboolib.module.nms.MinecraftConnection
 *
 * @author 坏黑
 * @since 2024/3/13 00:25
 */
interface MinecraftConnection {

    /** 获取连接的地址 */
    fun address(): InetSocketAddress {
        return nmsProxy<ConnectionGetter>().getAddress(this)
    }
}
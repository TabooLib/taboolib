package taboolib.module.nms

import io.netty.channel.Channel
import taboolib.common.util.unsafeLazy
import java.net.InetAddress

/**
 * TabooLib
 * taboolib.module.nms.ConnectionGetter
 *
 * @author 坏黑
 * @since 2023/1/31 20:50
 */
abstract class ConnectionGetter {

    abstract fun getConnection(address: InetAddress): Any

    abstract fun getChannel(connection: Any): Channel

    companion object {

        @JvmStatic
        val instance by unsafeLazy { nmsProxy<ConnectionGetter>() }
    }
}
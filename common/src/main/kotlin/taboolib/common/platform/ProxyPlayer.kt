package taboolib.common.platform

import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.common.platform.ProxyPlayer
 *
 * @author sky
 * @since 2021/6/17 12:03 上午
 */
interface ProxyPlayer : ProxyCommandSender {

    val address: InetSocketAddress?

    val uniqueId: UUID

    val ping: Int

    val locale: String

    fun kick(message: String?)

    fun chat(message: String)

    fun sendRawMessage(message: String)
}
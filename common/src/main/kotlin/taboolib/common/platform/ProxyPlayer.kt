package taboolib.common.platform

import taboolib.common.util.Location
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

    val world: String

    val location: Location

    fun kick(message: String?)

    fun chat(message: String)

    fun playSound(location: Location, sound: String, volume: Float, pitch: Float)

    fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float)

    fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int)

    fun sendActionBar(message: String)

    fun sendRawMessage(message: String)

    fun teleport(loc: Location)

}
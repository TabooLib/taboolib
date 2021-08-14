package taboolib.common.platform.service

import taboolib.common.platform.PlatformService
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer

/**
 * TabooLib
 * taboolib.common.platform.PlatformAdaptor
 *
 * @author sky
 * @since 2021/6/17 12:04 上午
 */
@PlatformService
interface PlatformAdapter {

    fun console(): ProxyCommandSender

    fun onlinePlayers(): List<ProxyPlayer>

    fun adaptPlayer(any: Any): ProxyPlayer

    fun adaptCommandSender(any: Any): ProxyCommandSender
}
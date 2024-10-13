package taboolib.platform

import net.afyer.afybroker.server.Broker
import net.afyer.afybroker.server.proxy.BrokerPlayer
import taboolib.common.Inject
import taboolib.common.platform.*
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import taboolib.platform.type.AfyBrokerCommandSender
import taboolib.platform.type.AfyBrokerPlayer

/**
 * TabooLib
 * taboolib.platform.AfyBrokerAdapter
 *
 * @author Ling556
 * @since 2024/5/09 23:51
 */
@Awake
@Inject
@PlatformSide(Platform.AFYBROKER)
class AfyBrokerAdapter : PlatformAdapter {

    override fun console(): ProxyCommandSender {
        return AfyBrokerCommandSender
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return Broker.getPlayerManager().players.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return AfyBrokerPlayer(any as BrokerPlayer)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return AfyBrokerCommandSender
    }

    override fun adaptLocation(any: Any): Location {
        error("Unsupported")
    }

    override fun platformLocation(location: Location): Any {
        error("Unsupported")
    }

    override fun allWorlds(): List<String> {
        error("Unsupported")
    }
}
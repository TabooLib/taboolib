package taboolib.platform

import de.dytanic.cloudnet.CloudNet
import de.dytanic.cloudnet.command.ICommandSender
import de.dytanic.cloudnet.driver.CloudNetDriver
import de.dytanic.cloudnet.ext.bridge.player.CloudPlayer
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager
import taboolib.common.platform.*
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import taboolib.platform.type.CloudNetV3CommandSender
import taboolib.platform.type.CloudNetV3Player
import taboolib.platform.type.sender

/**
 * TabooLib
 * taboolib.platform.BungeeAdapter
 *
 * @author CziSKY
 * @since 2021/6/21 14:28
 */
@Awake
@PlatformSide([Platform.CLOUDNET_V3])
class CloudNetV3Adapter : PlatformAdapter {

    val plugin by unsafeLazy { CloudNetV3Plugin.getInstance() }

    override fun console(): ProxyCommandSender {
        return adaptCommandSender(CloudNet.getInstance().consoleCommandSender)
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return CloudNetDriver.getInstance().servicesRegistry.getFirstService(IPlayerManager::class.java).onlinePlayers.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        if (any is CloudPlayer) {
            return CloudNetV3Player(any.sender)
        }
        return CloudNetV3Player(any as ICommandSender)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is CloudPlayer) {
            adaptPlayer(any)
        } else {
            CloudNetV3CommandSender(CloudNet.getInstance().consoleCommandSender)
        }
    }

    override fun adaptLocation(any: Any): Location {
        TODO("Not yet implemented")
    }

    override fun platformLocation(location: Location): Any {
        TODO("Not yet implemented")
    }

    override fun allWorlds(): List<String> {
        TODO("Not yet implemented")
    }
}
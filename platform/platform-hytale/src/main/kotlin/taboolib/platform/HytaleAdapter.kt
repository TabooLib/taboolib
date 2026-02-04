package taboolib.platform

import com.hypixel.hytale.math.vector.Transform
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.command.system.CommandSender
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.Universe
import taboolib.common.Inject
import taboolib.common.platform.*
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import taboolib.common.util.unsafeLazy
import taboolib.platform.type.HytaleCommandSender
import taboolib.platform.type.HytalePlayer

/**
 * TabooLib
 * taboolib.platform.HytaleAdapter
 *
 * @author sky
 * @since 2024/1/1
 */
@Suppress("removal")
@Awake
@Inject
@PlatformSide(Platform.HYTALE)
class HytaleAdapter : PlatformAdapter {

    val plugin by unsafeLazy { HytalePlugin.getInstance() }

    override fun console(): ProxyCommandSender {
        return HytaleCommandSender.Console
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        // 遍历所有世界，收集所有在线玩家
        return Universe.get().worlds.values.flatMap { world ->
            world.players.map { player -> HytalePlayer(player) }
        }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return HytalePlayer(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is Player) adaptPlayer(any) else HytaleCommandSender(any as CommandSender)
    }

    override fun adaptLocation(any: Any): Location {
        return when (any) {
            is Transform -> {
                val position = any.position
                val rotation = any.rotation
                Location(null, position.x, position.y, position.z, rotation.yaw, rotation.pitch)
            }
            is Vector3d -> {
                Location(null, any.x, any.y, any.z)
            }
            else -> error("Cannot adapt ${any::class.java.name} to Location")
        }
    }

    override fun platformLocation(location: Location): Any {
        return Transform(
            Vector3d(location.x, location.y, location.z),
            Vector3f(location.pitch, location.yaw, 0f)
        )
    }

    override fun allWorlds(): List<String> {
        return Universe.get().worlds.keys.toList()
    }
}

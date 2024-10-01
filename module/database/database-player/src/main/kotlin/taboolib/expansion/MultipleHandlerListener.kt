package taboolib.expansion

import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.Inject
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide

@Inject
@PlatformSide(Platform.BUKKIT)
object MultipleHandlerListener {

    val hooks = ArrayList<MultipleHandler>()

    @SubscribeEvent
    fun onPlayerJoin(event: PlayerJoinEvent) {
        hooks.forEach {
            it.setupDataContainer(event.player.uniqueId.toString())
        }
    }

    @SubscribeEvent
    fun onPlayerQuit(event: PlayerQuitEvent) {
        hooks.forEach {
            it.removeDataContainer(event.player.uniqueId.toString())
        }
    }
}

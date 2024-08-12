package taboolib.expansion

import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.Inject
import taboolib.common.platform.event.SubscribeEvent

@Inject
object MultipleHandlerListener {

    val hooks = ArrayList<MultipleHandler>()

    @SubscribeEvent
    fun onPlayerJoin(event: PlayerJoinEvent) {
        hooks.forEach {
            it.setupDataContainer(event.player.uniqueId.toString())
        }
    }

    @SubscribeEvent
    fun onPlayerQuit(event: PlayerJoinEvent) {
        hooks.forEach {
            it.removeDataContainer(event.player.uniqueId.toString())
        }
    }

}

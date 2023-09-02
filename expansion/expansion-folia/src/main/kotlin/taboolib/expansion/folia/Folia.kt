package taboolib.expansion.folia

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.expansion.folialib.FoliaLibAPI
import taboolib.platform.BukkitPlugin

object Folia {

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    val api by lazy {
        FoliaLibAPI()
    }

    fun teleport(entity: Entity, location: Location, cause: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN) {
        api.teleport(entity, location, cause)
    }

    fun cancelTask() {
        api.cancelTask(plugin)
    }

}

package taboolib.platform.event

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.Isolated
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Baffle
import java.util.concurrent.TimeUnit

/**
 * TabooLib
 * taboolib.platform.event.PlayerJumpEvent
 *
 * @author sky
 * @since 2021/7/18 12:11 下午
 */
@Isolated
class PlayerJumpEvent(val player: Player) : ProxyEvent() {

    @PlatformSide([Platform.BUKKIT])
    internal object Listener {

        val baffle = Baffle.of(350, TimeUnit.MILLISECONDS)

        @SubscribeEvent(priority = EventPriority.HIGH, ignoreCancelled = true)
        fun e(e: PlayerMoveEvent) {
            val to = e.to ?: return
            if (e.player.isFlying || e.player.gameMode == GameMode.SPECTATOR) {
                return
            }
            if (e.from.y + 0.5 != to.y && e.from.y + 0.419 < to.y && baffle.hasNext(e.player.name)) {
                if (!PlayerJumpEvent(e.player).call()) {
                    e.setTo(e.from)
                    baffle.reset(e.player.name)
                }
            }
        }

        @SubscribeEvent
        fun e(e: PlayerQuitEvent) {
            baffle.reset(e.player.name)
        }
    }
}
package taboolib.module.kether.action.game.bukkit

import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.util.asList
import taboolib.module.kether.*
import taboolib.module.nms.sendScoreboard

@PlatformSide([Platform.BUKKIT])
object ActionScoreboard {

    @KetherParser(["scoreboard"])
    fun actionScoreboard() = scriptParser {
        val value = it.nextParsedAction()
        actionNow {
            run(value).thenAccept { o ->
                val viewer = player().cast<Player>()
                if (o == null) {
                    viewer.sendScoreboard()
                } else {
                    val body = if (o is Collection<*> || o is Array<*>) o.asList() else o.toString().trimIndent().lines()
                    viewer.sendScoreboard(body[0], *body.filterIndexed { index, _ -> index > 0 }.toTypedArray())
                }
            }
        }
    }
}
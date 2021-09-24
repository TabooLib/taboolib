package taboolib.module.kether.action.game.bukkit

import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.util.asList
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.script
import taboolib.module.kether.scriptParser
import taboolib.module.nms.sendScoreboard
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionScoreboard(val content: ParsedAction<*>) : ScriptAction<Void>() {

    override fun run(frame: QuestContext.Frame): CompletableFuture<Void> {
        return frame.newFrame(content).run<Any>().thenAccept { content ->
            val viewer: Player = frame.script().sender?.castSafely<Player>() ?: error("No player selected.")
            if (content == null) {
                viewer.sendScoreboard()
            } else {
                val body = if (content is List<*>) content.asList() else content.toString().trimIndent().split('\n')
                viewer.sendScoreboard(body[0], *body.filterIndexed { index, _ -> index > 0 }.toTypedArray())
            }
        }
    }

    internal object Parser {

        /**
         * scoreboard *"123"
         */
        @PlatformSide([Platform.BUKKIT])
        @KetherParser(["scoreboard"])
        fun parser() = scriptParser {
            ActionScoreboard(it.nextAction<Any>())
        }
    }
}
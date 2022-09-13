package taboolib.module.kether.action.game

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionSubtitle(
    val subTitle: ParsedAction<*>,
    val fadeIn: ParsedAction<*>,
    val stay: ParsedAction<*>,
    val fadeOut: ParsedAction<*>,
) : ScriptAction<Void>() {

    override fun run(frame: QuestContext.Frame): CompletableFuture<Void> {
        frame.run(subTitle).str { s ->
            frame.run(fadeIn).int { fadeIn ->
                frame.run(stay).int { stay ->
                    frame.run(fadeOut).int { fadeOut ->
                        val viewer = frame.player()
                        viewer.sendTitle("§r", s.replace("@sender", viewer.name), fadeIn, stay, fadeOut)
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(null)
    }

    object Parser {

        @KetherParser(["subtitle"])
        fun parser() = scriptParser {
            val subTitle = it.nextParsedAction()
            var fadeIn = literalAction(0)
            var stay = literalAction(20)
            var fadeOut = literalAction(0)
            it.mark()
            try {
                it.expects("by", "with")
                fadeIn = it.nextParsedAction()
                stay = it.nextParsedAction()
                fadeOut = it.nextParsedAction()
            } catch (ignored: Exception) {
                it.reset()
            }
            ActionSubtitle(subTitle, fadeIn, stay, fadeOut)
        }
    }
}
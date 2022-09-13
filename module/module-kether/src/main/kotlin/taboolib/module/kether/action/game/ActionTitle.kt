package taboolib.module.kether.action.game

import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionTitle(
    val title: ParsedAction<*>,
    val subTitle: ParsedAction<*>,
    val fadeIn: ParsedAction<*>,
    val stay: ParsedAction<*>,
    val fadeOut: ParsedAction<*>,
) : ScriptAction<Void>() {

    override fun run(frame: QuestContext.Frame): CompletableFuture<Void> {
        frame.run(title).str { t ->
            frame.run(subTitle).str { s ->
                frame.run(fadeIn).int { fadeIn ->
                    frame.run(stay).int { stay ->
                        frame.run(fadeOut).int { fadeOut ->
                            val viewer = frame.player()
                            val title = t.replace("@sender", viewer.name)
                            val subTitle = s.replace("@sender", viewer.name)
                            viewer.sendTitle(title, subTitle, fadeIn, stay, fadeOut)
                        }
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(null)
    }

    object Parser {

        @KetherParser(["title"])
        fun parser() = scriptParser {
            val title = it.nextParsedAction()
            it.mark()
            val subTitle = try {
                it.expect("subtitle")
                it.nextParsedAction()
            } catch (ignored: Exception) {
                it.reset()
                literalAction("§r")
            }
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
            ActionTitle(title, subTitle, fadeIn, stay, fadeOut)
        }
    }
}
package taboolib.module.kether.action.game

import taboolib.common.platform.function.platformLocation
import taboolib.common.util.Location
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.Parser
import taboolib.library.kether.Parser.Action
import taboolib.library.kether.Parsers
import taboolib.library.kether.QuestContext
import taboolib.module.kether.*
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionLocation(
    val world: ParsedAction<*>,
    val x: ParsedAction<*>,
    val y: ParsedAction<*>,
    val z: ParsedAction<*>,
    val yaw: ParsedAction<*>,
    val pitch: ParsedAction<*>,
) : ScriptAction<Any?>() {

    override fun run(frame: QuestContext.Frame): CompletableFuture<Any?> {
        val future = CompletableFuture<Any?>()
        frame.run(world).str { world ->
            frame.run(x).double { x ->
                frame.run(y).double { y ->
                    frame.run(z).double { z ->
                        frame.run(yaw).float { yaw ->
                            frame.run(pitch).float { pitch ->
                                future.complete(platformLocation<Any>(Location(world, x, y, z, yaw, pitch)))
                            }
                        }
                    }
                }
            }
        }
        return future
    }

    object Parser {

        /**
         * location 10 20 10 and 0 0
         */
        // @KetherParser(["loc", "location"])
        fun parser() = scriptParser {
            val world = it.nextParsedAction()
            val x = it.nextParsedAction()
            val y = it.nextParsedAction()
            val z = it.nextParsedAction()
            var yaw = literalAction(0)
            var pitch = literalAction(0)
            it.mark()
            try {
                it.expect("and")
                yaw = it.nextParsedAction()
                pitch = it.nextParsedAction()
            } catch (ignored: Exception) {
                it.reset()
            }
            ActionLocation(world, x, y, z, yaw, pitch)
        }

        /**
         * location 10 20 10 and 0 0
         */
        @KetherParser(["loc", "location"])
        fun parser2() = parserCombinator {
            it.group(
                string(),
                double(),
                double(),
                double(),
                Parsers.command("and", float().and(float())).option()
            ).apply(it) { world, x, y, z, yap ->
                val (yaw, pitch) = yap ?: (0f to 0f)
                now { platformLocation<Location>(Location(world, x, y, z, yaw, pitch)) }
            }
        }
    }
}
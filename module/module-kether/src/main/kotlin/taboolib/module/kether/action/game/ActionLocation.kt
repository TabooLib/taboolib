package taboolib.module.kether.action.game

import taboolib.common.platform.function.platformLocation
import taboolib.common.util.Location
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.module.kether.*
import taboolib.module.kether.action.ActionLiteral
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
        return frame.run(world).str { world ->
            frame.run(x).double { x ->
                frame.run(y).double { y ->
                    frame.run(z).double { z ->
                        frame.run(yaw).float { yaw ->
                            frame.run(pitch).float { pitch ->
                                platformLocation<Any>(Location(world, x, y, z, yaw, pitch))
                            }.join()
                        }.join()
                    }.join()
                }.join()
            }.join()
        }
    }

    internal object Parser {

        /**
         * location 10 20 10 and 0 0
         */
        @KetherParser(["loc", "location"])
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
    }
}
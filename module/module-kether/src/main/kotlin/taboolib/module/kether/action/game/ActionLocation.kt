package taboolib.module.kether.action.game

import taboolib.common.platform.function.platformLocation
import taboolib.common.util.Location
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.library.kether.actions.LiteralAction
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.scriptParser
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
) : ScriptAction<Any>() {

    override fun run(frame: QuestContext.Frame): CompletableFuture<Any> {
        val location = CompletableFuture<Any>()
        frame.newFrame(world).run<Any>().thenApply { world ->
            frame.newFrame(x).run<Any>().thenApply { x ->
                frame.newFrame(y).run<Any>().thenApply { y ->
                    frame.newFrame(z).run<Any>().thenApply { z ->
                        frame.newFrame(yaw).run<Any>().thenApply { yaw ->
                            frame.newFrame(pitch).run<Any>().thenApply { pitch ->
                                val loc = Location(
                                    world.toString(),
                                    Coerce.toDouble(x),
                                    Coerce.toDouble(y),
                                    Coerce.toDouble(z),
                                    Coerce.toFloat(yaw),
                                    Coerce.toFloat(pitch)
                                )
                                location.complete(platformLocation<Any>(loc))
                            }
                        }
                    }
                }
            }
        }
        return location
    }

    internal object Parser {

        /**
         * location 10 20 10 and 0 0
         */
        @KetherParser(["loc", "location"])
        fun parser() = scriptParser {
            val world = it.next(ArgTypes.ACTION)
            val x = it.next(ArgTypes.ACTION)
            val y = it.next(ArgTypes.ACTION)
            val z = it.next(ArgTypes.ACTION)
            var yaw: ParsedAction<*> = ParsedAction(LiteralAction<Any>("0"))
            var pitch: ParsedAction<*> = ParsedAction(LiteralAction<Any>("0"))
            it.mark()
            try {
                it.expect("and")
                yaw = it.next(ArgTypes.ACTION)
                pitch = it.next(ArgTypes.ACTION)
            } catch (ignored: Exception) {
                it.reset()
            }
            ActionLocation(world, x, y, z, yaw, pitch)
        }
    }
}
package taboolib.module.kether.action.game

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestContext
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionSound(val sound: ParsedAction<*>, val volume: ParsedAction<*>, val pitch: ParsedAction<*>) : ScriptAction<Void>() {

    override fun run(frame: QuestContext.Frame): CompletableFuture<Void> {
        val viewer = frame.player()
        frame.run(sound).str { sound ->
            frame.run(volume).float { volume ->
                frame.run(pitch).float { pitch ->
                    if (sound.startsWith("resource:")) {
                        viewer.playSoundResource(viewer.location, sound.substringAfter("resource:"), volume, pitch)
                    } else {
                        viewer.playSound(viewer.location, sound.replace('.', '_').uppercase(), volume, pitch)
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(null)
    }

    object Parser {

        /**
         * sound block_stone_break by 1 1
         */
        @PlatformSide([Platform.BUKKIT])
        @KetherParser(["sound"])
        fun parser() = scriptParser {
            val sound = it.nextParsedAction()
            var volume = literalAction(1.0)
            var pitch = literalAction(1.0)
            it.mark()
            try {
                it.expects("by", "with")
                volume = it.nextParsedAction()
                pitch = it.nextParsedAction()
            } catch (ignored: Exception) {
                it.reset()
            }
            ActionSound(sound, volume, pitch)
        }
    }
}
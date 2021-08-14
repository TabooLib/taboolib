package taboolib.module.kether.action.game

import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionPlayer(val name: String, val operator: PlayerOperator, val method: PlayerOperator.Method, val value: ParsedAction<*>?) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        val viewer = frame.script().sender as? ProxyPlayer ?: error("No player selected.")
        return if (value != null) {
            frame.newFrame(value).run<Any>().thenApplyAsync({
                try {
                    operator.writer?.func?.invoke(viewer, method, it) ?: error("Player \"$name\" is not writable.")
                } catch (ex: NoSuchMethodError) {
                    viewer.sendMessage("§cPlayer \"$name\" is not supported for this minecraft version (or platform).")
                } catch (ex: NoSuchFieldError) {
                    viewer.sendMessage("§cPlayer \"$name\" is not supported for this minecraft version (or platform).")
                }
            }, frame.context().executor)
        } else {
            CompletableFuture.completedFuture(operator.reader?.func?.invoke(viewer) ?: error("Player \"$name\" is not readable."))
        }
    }

    internal object Parser {

        init {
            PlayerOperators.values().forEach { Kether.addPlayerOperator(it.name, it.build()) }
        }

        @KetherParser(["player"])
        fun parser() = scriptParser {
            it.mark()
            val tokens = arrayListOf(it.nextToken())
            val structure = Kether.registeredPlayerOperator.entries.firstOrNull { e ->
                val args = e.key.lowercase(Locale.getDefault()).split("_")
                var i = 0
                args.all { a ->
                    if (tokens.size < ++i) {
                        tokens.add(it.nextToken())
                    }
                    tokens[i - 1] == a
                }
            } ?: throw KetherError.NOT_PLAYER_OPERATOR.create(tokens.joinToString(" "))
            it.reset()
            // 恢复指针位置
            structure.key.split("_").forEach { _ ->
                it.nextToken()
            }
            it.mark()
            val method = if (it.hasNext()) {
                when (it.nextToken()) {
                    "to", "=" -> PlayerOperator.Method.MODIFY
                    "add", "increase", "+" -> PlayerOperator.Method.INCREASE
                    "sub", "decrease", "-" -> PlayerOperator.Method.DECREASE
                    else -> {
                        it.reset()
                        PlayerOperator.Method.NONE
                    }
                }
            } else {
                PlayerOperator.Method.NONE
            }
            if (method != PlayerOperator.Method.NONE) {
                if (method in structure.value.usable) {
                    ActionPlayer(structure.key, structure.value, method, it.next(ArgTypes.ACTION))
                } else {
                    error("Player \"${structure.key}\" is not supported for ${method.name.lowercase(Locale.getDefault())} method.")
                }
            } else {
                ActionPlayer(structure.key, structure.value, method, null)
            }
        }
    }
}
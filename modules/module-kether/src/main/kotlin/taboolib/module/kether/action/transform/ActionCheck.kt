package taboolib.module.kether.action.transform

import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import taboolib.module.kether.action.transform.ActionCheck.Symbol.*
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionCheck(val left: ParsedAction<*>, val right: ParsedAction<*>, val symbol: Symbol) : ScriptAction<Boolean>() {

    enum class Symbol {

        NOT_EQUALS, EQUALS, EQUALS_MEMORY, EQUALS_IGNORE_CASE, GT, GT_EQ, LT, LT_EQ
    }

    fun check(left: Any?, right: Any?): Boolean {
        return when (symbol) {
            EQUALS -> left.inferType() == right.inferType()
            EQUALS_MEMORY -> left === right
            EQUALS_IGNORE_CASE -> left.toString().equals(right.toString(), ignoreCase = true)
            NOT_EQUALS -> left != right
            GT -> Coerce.toDouble(left) > Coerce.toDouble(right)
            GT_EQ -> Coerce.toDouble(left) >= Coerce.toDouble(right)
            LT -> Coerce.toDouble(left) < Coerce.toDouble(right)
            LT_EQ -> Coerce.toDouble(left) <= Coerce.toDouble(right)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
        return CompletableFuture<Boolean>().also { future ->
            frame.newFrame(left).run<Any?>().thenAccept { left ->
                frame.newFrame(right).run<Any?>().thenAccept { right ->
                    future.complete(check(left, right))
                }
            }
        }
    }

    internal object Parser {

        @KetherParser(["check"])
        fun parser() = scriptParser {
            val left = it.next(ArgTypes.ACTION)
            val symbol = when (val type = it.nextToken()) {
                "==", "is" -> EQUALS
                "=!", "is!" -> EQUALS_MEMORY
                "=?", "is?" -> EQUALS_IGNORE_CASE
                "!=", "!is", "not" -> NOT_EQUALS
                ">", "gt" -> GT
                ">=" -> GT_EQ
                "<", "lt" -> LT
                "<=" -> LT_EQ
                else -> throw KetherError.NOT_SYMBOL.create(type)
            }
            val right = it.next(ArgTypes.ACTION)
            ActionCheck(left, right, symbol)
        }
    }
}
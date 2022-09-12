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

        NOT_EQUALS, EQUALS, EQUALS_NO_INFER, EQUALS_MEMORY, EQUALS_IGNORE_CASE, GT, GTE, LT, LTE, CONTAINS, IN
    }

    fun check(left: Any?, right: Any?): Boolean {
        return when (symbol) {
            EQUALS -> left.inferType() == right.inferType()
            EQUALS_NO_INFER -> left == right
            EQUALS_MEMORY -> left === right
            EQUALS_IGNORE_CASE -> left.toString().equals(right.toString(), ignoreCase = true)
            NOT_EQUALS -> left != right
            GT -> Coerce.toDouble(left) > Coerce.toDouble(right)
            GTE -> Coerce.toDouble(left) >= Coerce.toDouble(right)
            LT -> Coerce.toDouble(left) < Coerce.toDouble(right)
            LTE -> Coerce.toDouble(left) <= Coerce.toDouble(right)
            CONTAINS -> when (left) {
                is Collection<*> -> left.contains(right)
                is Array<*> -> left.contains(right)
                is Map<*, *> -> left.containsKey(right)
                else -> left.toString().contains(right.toString())
            }
            IN -> when (right) {
                is Collection<*> -> right.contains(left)
                is Array<*> -> right.contains(left)
                is Map<*, *> -> right.containsKey(left)
                else -> right.toString().contains(left.toString())
            }
        }
    }

    override fun run(frame: ScriptFrame): CompletableFuture<Boolean> {
        return CompletableFuture<Boolean>().also { future ->
            frame.newFrame(left).run<Any?>().thenAccept { left ->
                frame.newFrame(right).run<Any?>().thenAccept { right ->
                    future.complete(check(left, right))
                }
            }
        }
    }

    object Parser {

        @KetherParser(["check"])
        fun parser() = scriptParser {
            val left = it.next(ArgTypes.ACTION)
            val symbol = when (val type = it.nextToken()) {
                "==", "is" -> EQUALS
                "=!", "is!" -> EQUALS_NO_INFER
                "=!!", "is!!" -> EQUALS_MEMORY
                "=?", "is?" -> EQUALS_IGNORE_CASE
                "!=", "!is", "not" -> NOT_EQUALS
                ">", "gt" -> GT
                ">=", "gte" -> GTE
                "<", "lt" -> LT
                "<=", "lte" -> LTE
                "in" -> IN
                "contains", "has" -> CONTAINS
                else -> throw KetherError.NOT_SYMBOL.create(type)
            }
            val right = it.next(ArgTypes.ACTION)
            ActionCheck(left, right, symbol)
        }
    }
}
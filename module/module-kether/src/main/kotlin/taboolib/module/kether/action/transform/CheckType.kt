package taboolib.module.kether.action.transform

import taboolib.common5.Coerce
import taboolib.module.kether.KetherError
import taboolib.module.kether.inferType

enum class CheckType(val multi: Boolean, val check: (left: Any?, right: Any?) -> Boolean) {

    EQUALS(false, { left, right -> left.inferType() == right.inferType() }),

    EQUALS_NOT(false, { left, right -> left.inferType() != right.inferType() }),

    EQUALS_NO_INFER(false, { left, right -> left == right }),

    EQUALS_MEMORY(false, { left, right -> left === right }),

    EQUALS_IGNORE_CASE(false, { left, right -> left.toString().equals(right.toString(), ignoreCase = true) }),

    GT(false, { left, right -> Coerce.toDouble(left) > Coerce.toDouble(right) }),

    GTE(false, { left, right -> Coerce.toDouble(left) >= Coerce.toDouble(right) }),

    LT(false, { left, right -> Coerce.toDouble(left) < Coerce.toDouble(right) }),

    LTE(false, { left, right -> Coerce.toDouble(left) <= Coerce.toDouble(right) }),

    CONTAINS(true, { left, right ->
        when (left) {
            is Collection<*> -> left.contains(right)
            is Array<*> -> left.contains(right)
            is Map<*, *> -> left.containsKey(right)
            else -> left.toString().contains(right.toString())
        }
    }),

    IN(true, { left, right ->
        when (right) {
            is Collection<*> -> right.contains(left)
            is Array<*> -> right.contains(left)
            is Map<*, *> -> right.containsKey(left)
            else -> right.toString().contains(left.toString())
        }
    });

    companion object {

        fun fromStringSafely(token: String): CheckType? {
            return kotlin.runCatching { fromString(token) }.getOrNull()
        }

        fun fromString(token: String): CheckType {
            return when (token) {
                // 等价判断
                "==", "is" -> EQUALS
                "!=", "!is", "not" -> EQUALS_NOT
                // 特殊等价判断
                "=!", "is!" -> EQUALS_NO_INFER
                "=!!", "is!!" -> EQUALS_MEMORY
                "=?", "is?" -> EQUALS_IGNORE_CASE
                // 大小判断
                ">", "gt" -> GT
                ">=", "gte" -> GTE
                "<", "lt" -> LT
                "<=", "lte" -> LTE
                // 包含判断
                "in" -> IN
                "contains", "has" -> CONTAINS
                else -> throw KetherError.NOT_SYMBOL.create(token)
            }
        }
    }
}
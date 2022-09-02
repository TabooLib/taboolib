package taboolib.module.kether.action.game

import taboolib.common5.Coerce
import taboolib.module.kether.PlayerOperator

fun Int.modify(method: PlayerOperator.Method, v: Any?, max: Int? = null, min: Int? = null): Int {
    val value =  when (method) {
        PlayerOperator.Method.INCREASE -> this + Coerce.toInteger(v)
        PlayerOperator.Method.DECREASE -> this - Coerce.toInteger(v)
        PlayerOperator.Method.MODIFY -> Coerce.toInteger(v)
        else -> error("unsupported")
    }
    return when {
        max != null && value > max -> max
        min != null && value < min -> min
        else -> value
    }
}

fun Long.modify(method: PlayerOperator.Method, v: Any?, max: Long? = null, min: Long? = null): Long {
    val value = when (method) {
        PlayerOperator.Method.INCREASE -> this + Coerce.toLong(v)
        PlayerOperator.Method.DECREASE -> this - Coerce.toLong(v)
        PlayerOperator.Method.MODIFY -> Coerce.toLong(v)
        else -> error("unsupported")
    }
    return when {
        max != null && value > max -> max
        min != null && value < min -> min
        else -> value
    }
}

fun Float.modify(method: PlayerOperator.Method, v: Any?, max: Float = Float.NaN, min: Float = Float.NaN): Float {
    val value = when (method) {
        PlayerOperator.Method.INCREASE -> this + Coerce.toFloat(v)
        PlayerOperator.Method.DECREASE -> this - Coerce.toFloat(v)
        PlayerOperator.Method.MODIFY -> Coerce.toFloat(v)
        else -> error("unsupported")
    }
    return when {
        !max.isNaN() && value > max -> max
        !min.isNaN() && value < min -> min
        else -> value
    }
}

fun Double.modify(method: PlayerOperator.Method, v: Any?, max: Double = Double.NaN, min: Double = Double.NaN): Double {
    val value = when (method) {
        PlayerOperator.Method.INCREASE -> this + Coerce.toDouble(v)
        PlayerOperator.Method.DECREASE -> this - Coerce.toDouble(v)
        PlayerOperator.Method.MODIFY -> Coerce.toDouble(v)
        else -> error("unsupported")
    }
    return when {
        !max.isNaN() && value > max -> max
        !min.isNaN() && value < min -> min
        else -> value
    }
}
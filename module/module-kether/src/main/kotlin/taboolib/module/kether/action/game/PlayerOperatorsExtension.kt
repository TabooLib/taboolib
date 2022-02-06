package taboolib.module.kether.action.game

import taboolib.common5.Coerce
import taboolib.module.kether.PlayerOperator

fun Int.modify(method: PlayerOperator.Method, v: Any?): Int {
    return when (method) {
        PlayerOperator.Method.INCREASE -> {
            this + Coerce.toInteger(v)
        }
        PlayerOperator.Method.DECREASE -> {
            this - Coerce.toInteger(v)
        }
        PlayerOperator.Method.MODIFY -> {
            Coerce.toInteger(v)
        }
        else -> error("Unsupported")
    }
}

fun Long.modify(method: PlayerOperator.Method, v: Any?): Long {
    return when (method) {
        PlayerOperator.Method.INCREASE -> {
            this + Coerce.toLong(v)
        }
        PlayerOperator.Method.DECREASE -> {
            this - Coerce.toLong(v)
        }
        PlayerOperator.Method.MODIFY -> {
            Coerce.toLong(v)
        }
        else -> error("Unsupported")
    }
}

fun Float.modify(method: PlayerOperator.Method, v: Any?, max: Float = Float.NaN, min: Float = Float.NaN): Float {
    val value = when (method) {
        PlayerOperator.Method.INCREASE -> {
            this + Coerce.toFloat(v)
        }
        PlayerOperator.Method.DECREASE -> {
            this - Coerce.toFloat(v)
        }
        PlayerOperator.Method.MODIFY -> {
            Coerce.toFloat(v)
        }
        else -> error("Unsupported")
    }
    return when {
        value > max && !max.isNaN() -> max
        value < min && !min.isNaN() -> min
        else -> value
    }
}

fun Double.modify(method: PlayerOperator.Method, v: Any?, max: Double = Double.NaN, min: Double = Double.NaN): Double {
    val value = when (method) {
        PlayerOperator.Method.INCREASE -> {
            this + Coerce.toDouble(v)
        }
        PlayerOperator.Method.DECREASE -> {
            this - Coerce.toDouble(v)
        }
        PlayerOperator.Method.MODIFY -> {
            Coerce.toDouble(v)
        }
        else -> error("Unsupported")
    }
    return when {
        value > max && !max.isNaN() -> max
        value < min && !min.isNaN() -> min
        else -> value
    }
}
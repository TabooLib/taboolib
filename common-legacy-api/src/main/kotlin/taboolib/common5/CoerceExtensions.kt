@file:Suppress("SpellCheckingInspection")

package taboolib.common5

import java.math.RoundingMode

inline val Any?.cint: Int
    get() = Coerce.toInteger(this)

inline val Any?.cdouble: Double
    get() = Coerce.toDouble(this)

inline val Any?.cfloat: Float
    get() = Coerce.toFloat(this)

inline val Any?.clong: Long
    get() = Coerce.toLong(this)

inline val Any?.cshort: Short
    get() = Coerce.toShort(this)

inline val Any?.cbyte: Byte
    get() = Coerce.toByte(this)

inline val Any?.cchar: Char
    get() = Coerce.toChar(this)

inline val Any?.cbool: Boolean
    get() = Coerce.toBoolean(this)

fun Double.format(digits: Int = 2, roundingMode: RoundingMode = RoundingMode.HALF_UP): Double {
    return Coerce.format(this, digits, roundingMode)
}

fun Float.format(digits: Int = 2, roundingMode: RoundingMode = RoundingMode.HALF_UP): Float {
    return Coerce.format(this, digits, roundingMode)
}

infix fun String.eqic(other: String): Boolean {
    return this.equals(other, ignoreCase = true)
}
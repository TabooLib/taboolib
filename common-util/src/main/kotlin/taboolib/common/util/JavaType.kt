@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package taboolib.common.util

typealias JavaByte = java.lang.Byte

typealias JavaLong = java.lang.Long

typealias JavaDouble = java.lang.Double

typealias JavaFloat = java.lang.Float

typealias JavaShort = java.lang.Short

typealias JavaBoolean = java.lang.Boolean

val JavaPrimitiveInt: Class<Int>
    get() = Integer.TYPE

val JavaPrimitiveChar: Class<Char>
    get() = Character.TYPE

val JavaPrimitiveByte: Class<Byte>
    get() = java.lang.Byte.TYPE

val JavaPrimitiveLong: Class<Long>
    get() = java.lang.Long.TYPE

val JavaPrimitiveDouble: Class<Double>
    get() = java.lang.Double.TYPE

val JavaPrimitiveFloat: Class<Float>
    get() = java.lang.Float.TYPE

val JavaPrimitiveShort: Class<Short>
    get() = java.lang.Short.TYPE

val JavaPrimitiveBoolean: Class<Boolean>
    get() = java.lang.Boolean.TYPE

/**
 * 将原始类型转换为包装类型
 */
fun Class<*>.nonPrimitive(): Class<*> {
    return when (this) {
        JavaPrimitiveInt -> Integer::class.java
        JavaPrimitiveChar -> Character::class.java
        JavaPrimitiveByte -> JavaByte::class.java
        JavaPrimitiveLong-> JavaLong::class.java
        JavaPrimitiveDouble -> JavaDouble::class.java
        JavaPrimitiveFloat -> JavaFloat::class.java
        JavaPrimitiveShort -> JavaShort::class.java
        JavaPrimitiveBoolean -> JavaBoolean::class.java
        else -> this
    }
}
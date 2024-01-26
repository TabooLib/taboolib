package taboolib.common.util

/**
 * 将原始类型转换为包装类型
 */
fun Class<*>.nonPrimitive(): Class<*> {
    return when {
        this == Integer.TYPE -> Integer::class.java
        this == Character.TYPE -> Character::class.java
        this == java.lang.Byte.TYPE -> java.lang.Byte::class.java
        this == java.lang.Long.TYPE -> java.lang.Long::class.java
        this == java.lang.Double.TYPE -> java.lang.Double::class.java
        this == java.lang.Float.TYPE -> java.lang.Float::class.java
        this == java.lang.Short.TYPE -> java.lang.Short::class.java
        this == java.lang.Boolean.TYPE -> java.lang.Boolean::class.java
        else -> this
    }
}
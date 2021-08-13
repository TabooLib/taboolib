package taboolib.common.util

import java.util.function.Supplier

fun join(args: Array<String>, start: Int = 0, separator: String = " "): String {
    return args.filterIndexed { index, _ -> index >= start }.joinToString(separator)
}

fun <T> subList(list: List<T>, start: Int = 0, end: Int = list.size): List<T> {
    return list.filterIndexed { index, _ -> index in start until end }
}

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

fun <T> lazySupplier(supplier: () -> T): Supplier<T> {
    return object : Supplier<T> {

        val obj by lazy {
            supplier()
        }

        override fun get(): T {
            return obj
        }
    }
}

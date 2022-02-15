package taboolib.common.io

/**
 * TabooLib
 * taboolib.common.io.ExceptionInstGetter
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
abstract class InstGetter<T>(val source: Class<T>) {

    abstract fun get(): T?
}

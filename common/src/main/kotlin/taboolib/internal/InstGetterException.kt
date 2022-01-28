package taboolib.internal

import taboolib.common.io.InstGetter

/**
 * TabooLib
 * taboolib.internal.ExceptionInstGetter
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
class InstGetterException<T>(source: Class<T>, val throwable: Throwable) : InstGetter<T>(source) {

    override fun get(): T? {
        IllegalStateException("Exception getting an instance of $source", throwable).printStackTrace()
        return null
    }
}
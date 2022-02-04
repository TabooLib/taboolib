package taboolib.common.io

import taboolib.internal.Internal

/**
 * TabooLib
 * taboolib.common.io.ExceptionInstGetter
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
@Internal
class InstGetterException<T>(source: Class<T>, val throwable: Throwable) : InstGetter<T>(source) {

    override fun get(): T? {
        IllegalStateException("Exception getting an instance of $source", throwable).printStackTrace()
        return null
    }
}
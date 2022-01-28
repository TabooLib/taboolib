package taboolib.internal

import taboolib.common.io.InstGetter

/**
 * TabooLib
 * taboolib.internal.InstantInstGetter
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
class InstGetterInstant<T>(source: Class<T>, val instance: T) : InstGetter<T>(source) {

    override fun get(): T? {
        return instance
    }
}
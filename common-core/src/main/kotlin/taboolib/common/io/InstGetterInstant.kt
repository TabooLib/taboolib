package taboolib.common.io

import taboolib.internal.Internal

/**
 * TabooLib
 * taboolib.common.io.InstantInstGetter
 *
 * @author 坏黑
 * @since 2022/1/24 7:14 PM
 */
@Internal
class InstGetterInstant<T>(source: Class<T>, val instance: T) : InstGetter<T>(source) {

    override fun get(): T? {
        return instance
    }
}

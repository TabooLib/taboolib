package taboolib.module.configuration

import kotlin.reflect.KProperty

/**
 * TabooLib
 * taboolib.module.configuration.ConfigNodeTransfer
 *
 * @author sky
 * @since 2021/9/5 1:54 下午
 */
@Suppress("UNCHECKED_CAST")
class ConfigNodeTransfer<T, R>(internal val transfer: T.() -> R) {

    /** 是否为懒加载模式 */
    var isLazyMode = false
        private set

    /** 配置文件值 */
    internal var configValue: Any? = null
        private set

    /** 缓存值 */
    internal var cachedValue: Any? = null
        private set

    constructor(lazy: Boolean, transfer: T.() -> R) : this(transfer) {
        this.isLazyMode = lazy
    }

    /** 获取转换后的值 */
    fun get(): R {
        // 懒加载模式
        if (isLazyMode && cachedValue == null && configValue != null) {
            cachedValue = transfer(configValue as T)
        }
        return cachedValue as? R ?: error("No value")
    }

    /** 刷新缓存 */
    fun reset(configValue: Any) {
        // 懒加载模式
        if (isLazyMode) {
            this.cachedValue = null
            this.configValue = configValue
        } else {
            this.cachedValue = transfer(configValue as T)
        }
    }

    /** 代理属性 */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): R {
        return get()
    }
}

fun <T, R> conversion(lazy: Boolean = false, block: T.() -> R): ConfigNodeTransfer<T, R> {
    return ConfigNodeTransfer(lazy, block)
}

fun <T, R> lazyConversion(block: T.() -> R): ConfigNodeTransfer<T, R> {
    return ConfigNodeTransfer(true, block)
}
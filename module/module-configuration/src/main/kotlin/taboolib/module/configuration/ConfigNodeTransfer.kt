package taboolib.module.configuration

/**
 * TabooLib
 * taboolib.module.configuration.ConfigNodeTransfer
 *
 * @author sky
 * @since 2021/9/5 1:54 下午
 */
@Suppress("UNCHECKED_CAST")
class ConfigNodeTransfer<T, R>(internal val transfer: T.() -> R) {

    internal var value: Any? = null
        private set

    // 外部业务调用
    fun update(value: Any?) {
        this.value = transfer(value as T)
    }

    fun get(): R {
        return value!! as R
    }
}
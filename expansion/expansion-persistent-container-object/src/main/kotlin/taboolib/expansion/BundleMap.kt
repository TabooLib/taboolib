package taboolib.expansion

/**
 * TabooLib
 * taboolib.expansion.BundleMap
 *
 * @author 坏黑
 * @since 2023/3/29 14:21
 */
abstract class BundleMap {

    /** 获取数据 */
    abstract operator fun <T> get(name: String): T

    /** 获取数据 */
    abstract fun <T> getOrNull(name: String): T?
}
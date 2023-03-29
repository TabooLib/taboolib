package taboolib.expansion

/**
 * TabooLib
 * taboolib.expansion.BundleMapImpl
 *
 * @author 坏黑
 * @since 2023/3/29 14:27
 */
class BundleMapImpl(val map: Map<String, Any?>) : BundleMap() {

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(name: String): T {
        return map[name] as T
    }

    override fun <T> getOrNull(name: String): T? {
        return if (map.containsKey(name)) get(name) else null
    }
}
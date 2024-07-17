package taboolib.platform.util

import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue
import org.bukkit.metadata.Metadatable
import taboolib.platform.BukkitPlugin

/** 以标记执行 */
fun Metadatable.runMeta(key: String, value: Any = true, func: () -> Unit) {
    setMeta(key, value)
    try {
        func()
    } finally {
        removeMeta(key)
    }
}

fun Metadatable.setMeta(key: String, value: Any) {
    setMetadata(key, FixedMetadataValue(BukkitPlugin.getInstance(), value))
}

fun Metadatable.hasMeta(key: String): Boolean {
    return getMetaFirstOrNull(key) != null
}

fun Metadatable.getMeta(key: String): List<MetadataValue> {
    return getMetadata(key)
}

fun Metadatable.getMetaFirst(key: String): MetadataValue {
    return getMetadata(key).first()
}

fun Metadatable.getMetaFirstOrNull(key: String): MetadataValue? {
    return getMetadata(key).firstOrNull()
}

fun Metadatable.removeMeta(key: String) {
    removeMetadata(key, BukkitPlugin.getInstance())
}

inline fun <reified T> MetadataValue.cast(): T {
    return value() as T
}

inline fun <reified T> MetadataValue.castOrNull(): T? {
    return value() as? T
}
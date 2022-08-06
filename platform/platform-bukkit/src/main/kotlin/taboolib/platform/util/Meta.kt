@file:Isolated
package taboolib.platform.util

import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue
import org.bukkit.metadata.Metadatable
import taboolib.common.Isolated
import taboolib.platform.BukkitPlugin

fun Metadatable.setMeta(key: String, value: Any) {
    setMetadata(key, FixedMetadataValue(BukkitPlugin.getInstance(), value))
}

fun Metadatable.hasMeta(key: String): Boolean {
    return hasMetadata(key)
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
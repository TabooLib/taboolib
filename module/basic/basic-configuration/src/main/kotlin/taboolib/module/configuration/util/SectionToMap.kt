package taboolib.module.configuration.util

import taboolib.common5.cbool
import taboolib.common5.cbyte
import taboolib.common5.cchar
import taboolib.common5.cdouble
import taboolib.common5.cfloat
import taboolib.common5.cint
import taboolib.common5.clong
import taboolib.common5.cshort
import taboolib.library.configuration.ConfigurationSection

@Suppress("UNCHECKED_CAST")
inline fun <reified K, V> ConfigurationSection.getMap(path: String): Map<K, V> {
    val map = HashMap<K, V>()
    getConfigurationSection(path)?.let { section ->
        section.getKeys(false).forEach { key ->
            try {
                val convertedKey = convertKey<K>(key)
                map[convertedKey] = section[key] as V
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }
    return map
}

@Suppress("UNCHECKED_CAST")
inline fun <reified K> convertKey(key: String): K {
    return when (K::class) {
        Byte::class -> key.cbyte as K
        Short::class -> key.cshort as K
        Int::class -> key.cint as K
        Long::class -> key.clong as K
        Double::class -> key.cdouble as K
        Float::class -> key.cfloat as K
        Boolean::class -> key.cbool as K
        Char::class -> key.cchar as K
        else -> key as K
    }
}
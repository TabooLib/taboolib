package taboolib.module.configuration

import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import taboolib.internal.LocalCache.localFileMap

fun createLocal(path: String, saveTime: Long = 1200, type: Type? = null): Configuration {
    return localFileMap.computeIfAbsent(path) { Configuration.loadFromFile(newFile(getDataFolder(), path, create = true), type) }.apply {
        if (saveTime > 0) {
            submit(async = true, period = saveTime) { saveToFile(newFile(getDataFolder(), path, create = true)) }
        }
    }
}
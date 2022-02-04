@file:Isolated

package taboolib.module.configuration

import taboolib.common.Isolated
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private var init = true
private val files = ConcurrentHashMap<String, Configuration>()

fun createLocal(path: String, saveTime: Long = 1200, type: Type? = null): Configuration {
    if (files.containsKey(path)) {
        return files[path]!!
    }
    if (init) {
        init = false
        submit(period = saveTime, async = true) { Local.saveAll() }
    }
    return files.computeIfAbsent(path) { Configuration.loadFromFile(newFile(getDataFolder(), path, create = true), type) }
}

@Isolated
object Local {

    @Awake(LifeCycle.DISABLE)
    fun saveAll() {
        files.forEach { it.value.saveToFile(File(getDataFolder(), it.key)) }
    }
}
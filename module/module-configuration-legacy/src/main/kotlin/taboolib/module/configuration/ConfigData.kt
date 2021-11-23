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
private val files = ConcurrentHashMap<String, SecuredFile>()

fun createLocal(path: String, saveTime: Long = 1200): SecuredFile {
    if (files.containsKey(path)) {
        return files[path]!!
    }
    if (init) {
        init = false
        submit(period = saveTime, async = true) {
            Local.saveAll()
        }
    }
    return files.computeIfAbsent(path) { SecuredFile.loadConfiguration(newFile(getDataFolder(), path, create = true)) }
}

@Isolated
object Local {

    @Awake(LifeCycle.DISABLE)
    fun saveAll() {
        files.forEach { it.value.save(File(getDataFolder(), it.key)) }
    }
}
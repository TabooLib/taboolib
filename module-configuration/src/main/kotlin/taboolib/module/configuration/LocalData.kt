@file:Isolated

package taboolib.module.configuration

import taboolib.common.Isolated
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.getDataFolder
import taboolib.common.platform.submit
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private var created = false
private val files = ConcurrentHashMap<String, SecuredFile>()

fun createLocal(path: String): SecuredFile {
    if (files.containsKey(path)) {
        return files[path]!!
    }
    if (!created) {
        created = true
        submit(period = 1200, async = true) {
            Local.saveAll()
        }
    }
    return files.computeIfAbsent(path) { SecuredFile.loadConfiguration(File(getDataFolder(), path)) }
}

@Isolated
object Local {

    @Awake(LifeCycle.DISABLE)
    fun saveAll() {
        files.forEach { it.value.save(File(getDataFolder(), it.key)) }
    }
}
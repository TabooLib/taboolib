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
private val fileMap = ConcurrentHashMap<String, Configuration>()

/**
 * 创建一个本地数据文件
 *
 * @param path 文件路径
 * @param saveTime 自动保存时间
 * @param type 文件类型
 * @return [Configuration]
 */
fun createLocal(path: String, saveTime: Long = 1200, type: Type? = null): Configuration {
    if (fileMap.containsKey(path)) {
        return fileMap[path]!!
    }
    if (init) {
        init = false
        submit(period = saveTime, async = true) { Local.saveAll() }
    }
    return fileMap.computeIfAbsent(path) { Configuration.loadFromFile(newFile(getDataFolder(), path, create = true), type) }
}

@Isolated
object Local {

    @Awake(LifeCycle.DISABLE)
    fun saveAll() {
        fileMap.forEach { it.value.saveToFile(File(getDataFolder(), it.key)) }
    }
}
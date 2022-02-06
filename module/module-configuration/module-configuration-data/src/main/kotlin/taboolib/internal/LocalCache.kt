package taboolib.internal

import taboolib.common.platform.Awake
import taboolib.common.platform.Releasable
import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@Internal
@Awake
object LocalCache : Releasable {

    val localFileMap = ConcurrentHashMap<String, Configuration>()

    override fun release() {
        localFileMap.forEach { it.value.saveToFile(File(getDataFolder(), it.key)) }
    }
}
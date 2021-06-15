package taboolib.common.platform

import taboolib.common.TabooLibCommon
import taboolib.common.io.getClasses

object PlatformFactory {

    lateinit var platformIO: PlatformIO

    private val unnamedAPI = HashMap<String, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T> getAPI(name: String) = unnamedAPI[name] as? T ?: error("not found $name")

    @Suppress("NO_REFLECTION_IN_CLASS_PATH")
    fun init() {
        TabooLibCommon::class.java.protectionDomain.codeSource.location.getClasses().forEach {
            if (it.isAnnotationPresent(PlatformAPI::class.java)) {
                val interfaces = it.interfaces
                val instance = it.kotlin.objectInstance ?: it.getDeclaredConstructor().newInstance()
                when {
                    interfaces.contains(PlatformIO::class.java) -> {
                        platformIO = instance as PlatformIO
                    }
                    else -> {
                        unnamedAPI[it.simpleName] = instance
                    }
                }
            }
        }
    }
}
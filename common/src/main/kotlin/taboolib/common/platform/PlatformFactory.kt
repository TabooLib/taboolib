package taboolib.common.platform

import taboolib.common.TabooLibCommon
import taboolib.common.io.getClasses

object PlatformFactory {

    lateinit var platformIO: PlatformIO

    fun init() {
        TabooLibCommon::class.java.protectionDomain.codeSource.location.getClasses().forEach {
            if (it.isAnnotationPresent(PlatformAPI::class.java)) {
                val interfaces = it.interfaces
                val instance = it.getDeclaredConstructor().newInstance()
                when {
                    interfaces.contains(PlatformIO::class.java) -> {
                        platformIO = instance as PlatformIO
                    }
                }
            }
        }
    }
}
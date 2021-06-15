package taboolib.common.platform

import taboolib.common.env.KotlinEnv
import taboolib.common.io.classes

object PlatformFactory {

    lateinit var platformIO: PlatformIO
    lateinit var platformExecutor: PlatformExecutor

    private val unnamedAPI = HashMap<String, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T> getAPI(name: String) = unnamedAPI[name] as? T ?: error("not found $name")

    @Suppress("NO_REFLECTION_IN_CLASS_PATH")
    fun init() {
        if (KotlinEnv.isKotlinEnvironment()) {
            classes.forEach {
                if (it.isAnnotationPresent(PlatformInstance::class.java)) {
                    val interfaces = it.interfaces
                    val instance = it.kotlin.objectInstance ?: it.getDeclaredConstructor().newInstance()
                    when {
                        interfaces.contains(PlatformIO::class.java) -> {
                            platformIO = instance as PlatformIO
                        }
                        interfaces.contains(PlatformExecutor::class.java) -> {
                            platformExecutor = instance as PlatformExecutor
                        }
                        else -> {
                            unnamedAPI[it.simpleName] = instance
                        }
                    }
                }
            }
        }
    }
}
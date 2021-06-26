package taboolib.common.platform

import taboolib.common.TabooLibCommon
import taboolib.common.inject.Injector
import taboolib.common.inject.RuntimeInjector
import taboolib.common.io.classes

@Suppress("UNCHECKED_CAST", "NO_REFLECTION_IN_CLASS_PATH")
object PlatformFactory {

    lateinit var platformIO: PlatformIO
    lateinit var platformAdapter: PlatformAdapter
    lateinit var platformExecutor: PlatformExecutor
    lateinit var platformCommand: PlatformCommand

    private val awokenMap = HashMap<String, Any>()
    private val releaseTask = ArrayList<Releasable>()

    fun init() {
        if (TabooLibCommon.isKotlinEnvironment()) {
            classes.forEach {
                if (it.isAnnotationPresent(Awake::class.java) && checkPlatform(it)) {
                    val interfaces = it.interfaces
                    val instance = try {
                        it.kotlin.objectInstance ?: it.getDeclaredConstructor().newInstance()
                    } catch (ex: ExceptionInInitializerError) {
                        return@forEach
                    }
                    if (interfaces.contains(PlatformIO::class.java)) {
                        platformIO = instance as PlatformIO
                    }
                    if (interfaces.contains(PlatformAdapter::class.java)) {
                        platformAdapter = instance as PlatformAdapter
                    }
                    if (interfaces.contains(PlatformExecutor::class.java)) {
                        platformExecutor = instance as PlatformExecutor
                    }
                    if (interfaces.contains(PlatformCommand::class.java)) {
                        platformCommand = instance as PlatformCommand
                    }
                    if (interfaces.contains(Injector.Fields::class.java)) {
                        RuntimeInjector.register(instance as Injector.Fields)
                    }
                    if (interfaces.contains(Injector.Methods::class.java)) {
                        RuntimeInjector.register(instance as Injector.Methods)
                    }
                    if (interfaces.contains(Injector.Classes::class.java)) {
                        RuntimeInjector.register(instance as Injector.Classes)
                    }
                    if (interfaces.contains(Releasable::class.java)) {
                        releaseTask += instance as Releasable
                    }
                    awokenMap[it.simpleName] = instance
                }
            }
        }
    }

    /**
     * 注销方法
     */
    fun cancel() {
        unregisterCommands()
        releaseTask.forEach { it.release() }
    }

    /**
     * 检查指定类是否允许在当前平台运行
     */
    fun checkPlatform(clazz: Class<*>): Boolean {
        val platformSide = clazz.getAnnotation(PlatformSide::class.java) ?: return true
        return runningPlatform in platformSide.value
    }

    /**
     * 获取已被唤醒的 API 实例
     */
    fun <T> getAPI(name: String): T? {
        return awokenMap[name] as? T
    }
}
package taboolib.common.platform

import taboolib.common.OpenListener
import taboolib.common.TabooLibCommon
import taboolib.common.inject.Injector
import taboolib.common.inject.RuntimeInjector
import taboolib.common.io.getInstance
import taboolib.common.io.runningClasses

@Suppress("UNCHECKED_CAST", "NO_REFLECTION_IN_CLASS_PATH")
object PlatformFactory {

    lateinit var platformIO: PlatformIO
        private set
    lateinit var platformAdapter: PlatformAdapter
        private set
    lateinit var platformExecutor: PlatformExecutor
        private set
    lateinit var platformCommand: PlatformCommand
        private set

    private val awokenMap = HashMap<String, Any>()
    private val releaseTask = ArrayList<Releasable>()

    val openListener = ArrayList<OpenListener>()

    fun init() {
        if (TabooLibCommon.isKotlinEnvironment()) {
            runningClasses.forEach {
                TabooLibCommon.ENV.inject(it)
            }
            runningClasses.forEach {
                if (it.isAnnotationPresent(Awake::class.java) && checkPlatform(it)) {
                    val interfaces = it.interfaces
                    val instance = it.getInstance(true)?.get() ?: return@forEach
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
                    if (interfaces.contains(OpenListener::class.java)) {
                        openListener += instance as OpenListener
                    }
                    awokenMap[it.simpleName] = instance
                }
                if (it.isAnnotationPresent(PlatformImplementation::class.java) && it.getAnnotation(PlatformImplementation::class.java).platform == runningPlatform) {
                    val interfaces = it.interfaces
                    if (interfaces.isNotEmpty()) {
                        awokenMap[interfaces[0].simpleName] = it.getInstance(true)?.get() ?: return@forEach
                    }
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
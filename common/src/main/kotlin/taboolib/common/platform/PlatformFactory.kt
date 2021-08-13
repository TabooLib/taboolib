package taboolib.common.platform

import taboolib.common.OpenListener
import taboolib.common.TabooLibCommon
import taboolib.common.env.RuntimeEnv
import taboolib.common.inject.Injector
import taboolib.common.inject.RuntimeInjector
import taboolib.common.io.getInstance
import taboolib.common.io.runningClasses

@Suppress("UNCHECKED_CAST", "NO_REFLECTION_IN_CLASS_PATH")
object PlatformFactory {

    val awokenMap = HashMap<String, Any>()

    val serviceMap = HashMap<String, Any>()

    val releaseTask = ArrayList<Releasable>()

    val openListener = ArrayList<OpenListener>()

    fun init() {
        if (TabooLibCommon.isKotlinEnvironment()) {
            runningClasses.forEach {
                kotlin.runCatching {
                    RuntimeEnv.ENV.inject(it)
                }
            }
            runningClasses.forEach {
                if (it.isAnnotationPresent(Awake::class.java) && checkPlatform(it)) {
                    val interfaces = it.interfaces
                    val instance = it.getInstance(true)?.get() ?: return@forEach
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
                    interfaces.forEach { int ->
                        if (int.isAnnotationPresent(PlatformService::class.java)) {
                            serviceMap[int.simpleName] = instance
                        }
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
    inline fun <reified T> getAPI(): T {
        return awokenMap[T::class.java.simpleName] as T
    }

    /**
     * 获取已注册的跨平台服务
     */
    inline fun <reified T> getService(): T {
        return serviceMap[T::class.java.simpleName] as T
    }
}
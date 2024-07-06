package taboolib.common.platform

import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.PrimitiveSettings
import taboolib.common.TabooLib
import taboolib.common.env.RuntimeEnv
import taboolib.common.inject.ClassVisitor
import taboolib.common.inject.VisitorHandler
import taboolib.common.io.*
import taboolib.common.platform.function.registerLifeCycleTask
import taboolib.common.platform.function.unregisterCommands
import taboolib.common.reflect.hasAnnotation
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
object PlatformFactory {

    /** 已被唤醒的类 */
    val awokenMap: ConcurrentHashMap<String, Any>
        get() = TabooLib.getAwakenedClasses() as ConcurrentHashMap

    /** 已注册的服务 */
    val serviceMap = ConcurrentHashMap<String, Any>()

    @JvmStatic
    private fun init() {
        // 在 CONST 生命周期下注册优先级为 0 的任务
        registerLifeCycleTask(LifeCycle.CONST) {
            // 注册 Awake 接口
            try {
                LifeCycle.values().forEach { VisitorHandler.register(AwakeFunction(it)) }
            } catch (_: NoClassDefFoundError) {
            }

            // 获取所有运行类
            val markedClasses = VisitorHandler.getClasses().filter { classMarkers.match(it) }

            // 开发环境
            if (PrimitiveSettings.IS_DEBUG_MODE) {
                val time = System.currentTimeMillis()
                PrimitiveIO.debug("RunningClasses = ${runningClasses.size}")
                PrimitiveIO.debug("RunningClasses (Jar) = ${runningClassMapInJar.size}")
                PrimitiveIO.debug("RunningClasses (Public) = ${runningExactClasses.size}")
                PrimitiveIO.debug("RunningClasses (WithoutLibrary) = ${runningClassesWithoutLibrary.size}")
                PrimitiveIO.debug("RunningClasses (Marked) = ${markedClasses.size}")
                PrimitiveIO.debug("${System.currentTimeMillis() - time}ms")
            }

            // 加载运行环境
            markedClasses.parallelStream().forEach {
                try {
                    val total = RuntimeEnv.ENV.inject(it)
                    if (total > 0) {
                        classMarkers.mark(it)
                    }
                } catch (_: NoClassDefFoundError) {
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            }

            // 加载接口
            markedClasses.parallelStream().forEach {
                if (it.hasAnnotation(Awake::class.java) && checkPlatform(it)) {
                    val interfaces = it.interfaces
                    val instance = it.getInstance(true)?.get() ?: return@forEach
                    // 依赖注入接口
                    if (ClassVisitor::class.java.isAssignableFrom(it)) {
                        VisitorHandler.register(instance as ClassVisitor)
                    }
                    // 平台服务
                    interfaces.forEach { int ->
                        if (int.hasAnnotation(PlatformService::class.java)) {
                            serviceMap[int.name] = instance
                        }
                    }
                    awokenMap[it.name] = instance
                    classMarkers.mark(it)
                }
                // 平台实现
                if (it.hasAnnotation(PlatformImplementation::class.java) && it.getAnnotation(PlatformImplementation::class.java).platform == Platform.CURRENT) {
                    val interfaces = it.interfaces
                    if (interfaces.isNotEmpty()) {
                        awokenMap[interfaces[0].name] = it.getInstance(true)?.get() ?: return@forEach
                        classMarkers.mark(it)
                    }
                }
            }

            // 开发环境
            if (PrimitiveSettings.IS_DEBUG_MODE) {
                PrimitiveIO.debug("Service = ${serviceMap.size}")
                serviceMap.forEach { (k, v) ->
                    PrimitiveIO.debug(" = $k -> $v")
                }
            }
        }
        // 在 DISABLE 生命周期下注册优先级为 1 的任务
        registerLifeCycleTask(LifeCycle.DISABLE, 1) {
            runCatching { unregisterCommands() }
            runCatching {
                awokenMap.values.forEach {
                    if (it is Releasable) {
                        it.release()
                    }
                }
            }
        }
    }

    /**
     * 获取已被唤醒的 API 实例
     */
    inline fun <reified T> getAPI(): T = getAPI(T::class.java.name)

    /**
     * 获取已被唤醒的 API 实例（可能为空）
     */
    inline fun <reified T> getAPIOrNull() = awokenMap[T::class.java.name] as? T

    /**
     * 获取已被唤醒的 API 实例
     */
    fun <T> getAPI(name: String) = (awokenMap[name] ?: error("API ($name) not found, currently: ${awokenMap.keys}")) as T

    /**
     * 获取已注册的跨平台服务
     */
    inline fun <reified T> getService(): T = getService(T::class.java.name)

    /**
     * 获取已注册的跨平台服务（可能为空）
     */
    inline fun <reified T> getServiceOrNull() = serviceMap[T::class.java.name] as? T

    /**
     * 获取已注册的跨平台服务
     */
    fun <T> getService(name: String) = (serviceMap[name] ?: error("Service ($name) not found, currently: ${serviceMap.keys}")) as T

    /**
     * 注册 API 实例
     */
    inline fun <reified T : Any> registerAPI(instance: T) {
        awokenMap[T::class.java.name] = instance
    }

    /**
     * 注册跨平台服务
     */
    inline fun <reified T : Any> registerService(instance: T) {
        serviceMap[T::class.java.name] = instance
    }
}
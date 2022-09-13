package taboolib.common.platform

import taboolib.common.LifeCycle
import taboolib.common.TabooLibCommon
import taboolib.common.env.RuntimeEnv
import taboolib.common.inject.ClassVisitor
import taboolib.common.inject.VisitorHandler
import taboolib.common.io.getInstance
import taboolib.common.io.runningClasses
import taboolib.common.io.runningClassesWithoutLibrary
import taboolib.common.io.runningExactClasses
import taboolib.common.platform.function.runningPlatform
import taboolib.common.platform.function.unregisterCommands
import java.util.concurrent.ConcurrentHashMap

object PlatformFactory {

    val awokenMap = ConcurrentHashMap<String, Any>()

    val serviceMap = ConcurrentHashMap<String, Any>()

    fun init() {
        if (TabooLibCommon.isKotlinEnvironment()) {
            // 注册 Awake 接口
            try {
                LifeCycle.values().forEach { VisitorHandler.register(AwakeFunction(it)) }
            } catch (_: NoClassDefFoundError) {
            }

            // 开发环境
            if (TabooLibCommon.isDevelopmentMode()) {
                val time = System.currentTimeMillis()
                TabooLibCommon.print("RunningClasses = ${runningClasses.size}")
                TabooLibCommon.print("RunningClasses (Exact) = ${runningExactClasses.size}")
                TabooLibCommon.print("RunningClasses (WithoutLibrary) = ${runningClassesWithoutLibrary.size}")
                TabooLibCommon.print("${System.currentTimeMillis() - time}ms")
            }

            // 加载运行环境
            runningClassesWithoutLibrary.parallelStream().forEach {
                kotlin.runCatching { RuntimeEnv.ENV.inject(it) }.exceptionOrNull()?.takeIf { it !is NoClassDefFoundError }?.printStackTrace()
            }

            // 加载接口
            runningClassesWithoutLibrary.parallelStream().forEach {
                if (it.isAnnotationPresent(Awake::class.java) && checkPlatform(it)) {
                    val interfaces = it.interfaces
                    val instance = it.getInstance(true)?.get() ?: return@forEach
                    // 依赖注入接口
                    if (ClassVisitor::class.java.isAssignableFrom(it)) {
                        VisitorHandler.register(instance as ClassVisitor)
                    }
                    // 平台服务
                    interfaces.forEach { int ->
                        if (int.isAnnotationPresent(PlatformService::class.java)) {
                            serviceMap[int.name] = instance
                        }
                    }
                    awokenMap[it.name] = instance
                }
                // 平台实现
                if (it.isAnnotationPresent(PlatformImplementation::class.java) && it.getAnnotation(PlatformImplementation::class.java).platform == runningPlatform) {
                    val interfaces = it.interfaces
                    if (interfaces.isNotEmpty()) {
                        awokenMap[interfaces[0].name] = it.getInstance(true)?.get() ?: return@forEach
                    }
                }
            }

            // 开发环境
            if (TabooLibCommon.isDevelopmentMode()) {
                TabooLibCommon.print("Service = ${serviceMap.size}")
                serviceMap.forEach { (k, v) ->
                    TabooLibCommon.print(" = $k -> $v")
                }
            }
        }
    }

    /**
     * 注销方法
     */
    fun cancel() {
        kotlin.runCatching { unregisterCommands() }
        kotlin.runCatching {
            awokenMap.values.forEach {
                if (it is Releasable) {
                    it.release()
                }
            }
        }
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
        return awokenMap[T::class.java.name] as? T ?: error("API (${T::class.java.name}) not found, currently: ${awokenMap.keys}")
    }

    /**
     * 获取已注册的跨平台服务
     */
    inline fun <reified T> getService(): T {
        return serviceMap[T::class.java.name] as? T ?: error("Service (${T::class.java}) not found, currently: ${serviceMap.keys}")
    }

    /**
     * 注册 API
     */
    inline fun <reified T : Any> registerAPI(instance: Any) {
        awokenMap[T::class.java.name] = instance
    }

    /**
     * 注册跨平台服务
     */
    inline fun <reified T : Any> registerService(instance: Any) {
        serviceMap[T::class.java.name] = instance
    }
}
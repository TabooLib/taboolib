package taboolib.common.platform

import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.PrimitiveSettings
import taboolib.common.TabooLib
import taboolib.common.env.RuntimeEnv
import taboolib.common.inject.ClassVisitor
import taboolib.common.inject.ClassVisitorHandler
import taboolib.common.io.runningClassMapInJar
import taboolib.common.io.runningClasses
import taboolib.common.io.runningClassesWithoutLibrary
import taboolib.common.io.runningExactClasses
import taboolib.common.platform.function.registerLifeCycleTask
import taboolib.common.platform.function.unregisterCommands
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
                LifeCycle.values().forEach { ClassVisitorHandler.register(ClassVisitorAwake(it)) }
            } catch (_: NoClassDefFoundError) {
            }

            // 获取所有运行类
            val includedClasses = ClassVisitorHandler.getClasses()

            // 开发环境
            if (PrimitiveSettings.IS_DEBUG_MODE) {
                PrimitiveIO.debug("{0}ms", TabooLib.execution {
                    PrimitiveIO.debug("RunningClasses (All)            : {0}", runningClasses.size)
                    PrimitiveIO.debug("RunningClasses (Jar)            : {0}", runningClassMapInJar.size)
                    PrimitiveIO.debug("RunningClasses (Exact)          : {0}", runningExactClasses.size)
                    PrimitiveIO.debug("RunningClasses (WithoutLibrary) : {0}", runningClassesWithoutLibrary.size)
                    PrimitiveIO.debug("RunningClasses (Included)       : {0}", includedClasses.size)
                })
            }

            val time = System.nanoTime()
            var injected = 0
            // 加载运行环境
            for (cls in includedClasses) {
                try {
                    injected += RuntimeEnv.ENV.inject(cls)
                } catch (_: NoClassDefFoundError) {
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            }

            // 加载接口
            for (cls in includedClasses) {
                // 插件实例
                if (cls.structure.superclass?.name == Plugin::class.java.name) {
                    Plugin.setInstance((cls.getInstance() ?: cls.newInstance()) as Plugin)
                }
                // 自唤醒
                if (cls.hasAnnotation(Awake::class.java)) {
                    val instance = cls.getInstance() ?: cls.newInstance()
                    if (instance != null) {
                        // 依赖注入接口
                        if (ClassVisitor::class.java.isInstance(instance)) {
                            ClassVisitorHandler.register(instance as ClassVisitor)
                        }
                        // 平台服务
                        cls.interfaces.filter { it.hasAnnotation(PlatformService::class.java) }.forEach {
                            serviceMap[it.name!!] = instance
                        }
                        awokenMap[cls.name!!] = instance
                    } else {
                        PrimitiveIO.error("Failed to awake class: ${cls.name}")
                    }
                }
            }

            // 调试信息
            if (PrimitiveSettings.IS_DEBUG_MODE) {
                PrimitiveIO.debug("PlatformFactory initialized. ({0}ms)", (System.nanoTime() - time) / 1_000_000)
                PrimitiveIO.debug("Awakened: {0}", awokenMap.size)
                PrimitiveIO.debug("Injected: {0}", injected)
                PrimitiveIO.debug("Service : {0}", serviceMap.size)
                serviceMap.forEach { (k, v) ->
                    PrimitiveIO.debug(" = {0} ({1})", k.substringAfterLast('.'), v.javaClass.simpleName)
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
    fun <T> getAPI(name: String) = (awokenMap[name] ?: error("API ($name) not found, currently: ${awokenMap.keys}")) as T

    /**
     * 获取已注册的跨平台服务
     */
    fun <T> getService(name: String) = (serviceMap[name] ?: error("Service ($name) not found, currently: ${serviceMap.keys}")) as T

    /**
     * 获取已被唤醒的 API 实例
     */
    inline fun <reified T> getAPI(): T = getAPI(T::class.java.name)

    /**
     * 获取已被唤醒的 API 实例（可能为空）
     */
    inline fun <reified T> getAPIOrNull() = awokenMap[T::class.java.name] as? T

    /**
     * 获取已注册的跨平台服务
     */
    inline fun <reified T> getService(): T = getService(T::class.java.name)

    /**
     * 获取已注册的跨平台服务（可能为空）
     */
    inline fun <reified T> getServiceOrNull() = serviceMap[T::class.java.name] as? T

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
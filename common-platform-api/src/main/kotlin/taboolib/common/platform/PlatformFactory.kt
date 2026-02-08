package taboolib.common.platform

import org.tabooproject.reflex.ReflexClass
import org.tabooproject.reflex.serializer.BinaryReader
import org.tabooproject.reflex.serializer.BinaryWriter
import taboolib.common.*
import taboolib.common.env.RuntimeEnv
import taboolib.common.inject.ClassVisitor
import taboolib.common.inject.ClassVisitorHandler
import taboolib.common.io.*
import taboolib.common.platform.function.registerLifeCycleTask
import taboolib.common.platform.function.unregisterCommands
import taboolib.common.util.t
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
            // 是否有缓存
            val useCache = BinaryCache.read("inject/platform", BinaryCache.primarySrcVersion) { injectByCache(it, time) }
            if (useCache == null) {
                inject(includedClasses, time)
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

    private fun injectByCache(bytes: ByteArray, time: Long) {
        val reader = BinaryReader(bytes)
        // 依赖注入
        reader.readList { reader.readString() }.forEach { RuntimeEnv.ENV.inject(runningClassMap[it]!!) }

        // 代理主类
        val mainName = reader.readNullableString()
        if (mainName != null) {
            val main = runningClassMap[mainName]!!
            Plugin.setInstance((main.getInstance() ?: main.newInstance()) as Plugin)
        }

        // 自唤醒
        reader.readList {
            AwakeClass(reader.readString(), reader.readBoolean(), reader.readList { reader.readString() })
        }.forEach {
            val cls = runningClassMap[it.name]!!
            val instance = cls.getInstance() ?: cls.newInstance()
            // 是依赖注入接口
            if (it.isClassVisitor) {
                ClassVisitorHandler.register(instance as ClassVisitor)
            }
            // 是平台服务
            it.platformService.forEach { name ->
                serviceMap[name] = instance!!
            }
            awokenMap[it.name] = instance!!
        }

        // 调试信息
        if (PrimitiveSettings.IS_DEBUG_MODE) {
            PrimitiveIO.debug("跨平台服务初始化完成，用时 {0} 毫秒。(使用 BinaryCache）", (System.nanoTime() - time) / 1_000_000)
        }
    }

    private fun inject(includedClasses: Set<ReflexClass>, time: Long) {
        var injected = 0
        val writer = BinaryWriter()

        val envList = arrayListOf<String>()
        var main: String? = null
        val awakeClassList = arrayListOf<AwakeClass>()

        // 加载运行环境
        for (cls in includedClasses) {
            try {
                val i = RuntimeEnv.ENV.inject(cls)
                if (i > 0) {
                    injected += i
                    envList += cls.name!!
                }
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
                main = cls.name
            }
            // 自唤醒
            if (cls.hasAnnotation(Awake::class.java)) {
                val instance = cls.getInstance() ?: cls.newInstance()
                if (instance != null) {
                    // 依赖注入接口
                    var isClassVisitor = false
                    if (ClassVisitor::class.java.isInstance(instance)) {
                        isClassVisitor = true
                        ClassVisitorHandler.register(instance as ClassVisitor)
                    }
                    // 平台服务
                    val platformService = arrayListOf<String>()
                    cls.interfaces.filter { it.hasAnnotation(PlatformService::class.java) }.forEach {
                        platformService += it.name!!
                        serviceMap[it.name!!] = instance
                    }
                    awokenMap[cls.name!!] = instance
                    awakeClassList += AwakeClass(cls.name!!, isClassVisitor, platformService)
                } else {
                    PrimitiveIO.error(
                        """
                            无法激活 ${cls.name} 的 @Awake 注解
                            Failed to enforce @Awake annotation on ${cls.name}
                        """.t()
                    )
                }
            }
        }

        // 写入缓存
        writer.writeList(envList) { writer.writeNullableString(it) }
        writer.writeNullableString(main)
        writer.writeList(awakeClassList)

        // 保存缓存
        BinaryCache.save("inject/platform", BinaryCache.primarySrcVersion, writer.toByteArray())

        // 调试信息
        if (PrimitiveSettings.IS_DEBUG_MODE) {
            PrimitiveIO.debug("跨平台服务初始化完成，用时 {0} 毫秒。", (System.nanoTime() - time) / 1_000_000)
            PrimitiveIO.debug("  唤醒: {0}", awokenMap.size)
            PrimitiveIO.debug("  注入: {0}", injected)
            PrimitiveIO.debug("  服务: {0}", serviceMap.size)
            serviceMap.forEach { (k, v) ->
                PrimitiveIO.debug(" = {0} ({1})", k.substringAfterLast('.'), v.javaClass.simpleName)
            }
        }
    }
}
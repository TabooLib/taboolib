package taboolib.module.incision.gate

import taboolib.module.incision.diagnostic.Forensics

/**
 * Gate 定位器 — 负责"接入现有网关 + 版本协商"。
 *
 * 系统 ClassLoader 上的 holder 类 `taboolib.incision.gate.IncisionGate$V<N>` 仅作为发现锚点，
 * 真正的 gate 实现（DefaultIncisionGate）存储在 holder 的 `delegate` 字段中。
 *
 * 流程（由 [IncisionBootstrap] 在 ENABLE 阶段调用）：
 * 1. 查系统 ClassLoader 上是否存在 holder 类
 * 2. 不存在 → 通过 [GateBootstrapper] 自己创建
 * 3. 存在 → 从 holder 取 delegate，通过 Proxy 桥接跨 CL 调用
 */
object IncisionGateLocator {

    /** 查找并接入网关；若 JVM 上不存在则自建 */
    fun locateOrCreate(apiVersion: Int): IncisionGateApi {
        val existing = findExistingGate(apiVersion)
        if (existing != null) {
            Forensics.info("Gate located: apiVersion=$apiVersion reused=${existing.javaClass.name}")
            return existing
        }
        val created = GateBootstrapper.bootstrap(apiVersion)
        Forensics.info("Gate created: apiVersion=${created.apiVersion()}")
        return created
    }

    /** 扫描系统 ClassLoader 寻找已有网关实例（任何 API 版本） */
    fun listGates(): List<IncisionGateApi> {
        val sys = ClassLoader.getSystemClassLoader()
        val out = mutableListOf<IncisionGateApi>()
        for (v in 1..MAX_SUPPORTED_VERSION) {
            val delegate = getDelegateFromHolder(sys, v) ?: continue
            out += wrap(delegate, v) ?: continue
        }
        return out
    }

    private fun findExistingGate(apiVersion: Int): IncisionGateApi? {
        val sys = ClassLoader.getSystemClassLoader()
        // 优先查相同 API 版本
        val delegate = getDelegateFromHolder(sys, apiVersion)
        if (delegate != null) return wrap(delegate, apiVersion)

        // 再查更高版本（向前兼容）
        for (v in (apiVersion + 1)..MAX_SUPPORTED_VERSION) {
            val higher = getDelegateFromHolder(sys, v)
            if (higher != null) {
                Forensics.warn("Gate apiVersion=$v 已存在；本插件 api=$apiVersion 接入兼容子集")
                return wrap(higher, v)
            }
        }
        return null
    }

    /** 从系统 CL 的 holder 类中取 delegate 对象 */
    private fun getDelegateFromHolder(sys: ClassLoader, apiVersion: Int): Any? {
        val cls = try {
            Class.forName("taboolib.incision.gate.IncisionGate\$V$apiVersion", false, sys)
        } catch (_: Throwable) { return null }
        return try {
            cls.getMethod("getDelegate").invoke(null)
        } catch (_: Throwable) { null }
    }

    private fun wrap(instance: Any, apiVersion: Int): IncisionGateApi? {
        if (instance is IncisionGateApi) return instance
        return try {
            java.lang.reflect.Proxy.newProxyInstance(
                IncisionGateApi::class.java.classLoader,
                arrayOf(IncisionGateApi::class.java),
            ) { _, method, args ->
                val target = instance.javaClass.methods.firstOrNull { it.name == method.name && it.parameterCount == (args?.size ?: 0) }
                    ?: throw RuntimeException("gate 缺少方法 ${method.name}")
                try {
                    target.invoke(instance, *(args ?: emptyArray()))
                } catch (e: java.lang.reflect.InvocationTargetException) {
                    throw e.cause ?: e
                }
            } as IncisionGateApi
        } catch (t: Throwable) {
            Forensics.error("wrap gate 失败", t); null
        }
    }

    private const val MAX_SUPPORTED_VERSION = 9
}

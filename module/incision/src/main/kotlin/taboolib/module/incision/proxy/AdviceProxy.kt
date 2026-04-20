package taboolib.module.incision.proxy

import taboolib.module.incision.api.MethodCoordinate
import taboolib.module.incision.runtime.AdviceEntry

/**
 * 跨 ClassLoader advice 代理 — 网关持有的"非类型化"接口对象。
 *
 * 网关不能直接持有 [AdviceEntry]（含 Kotlin 类型），跨 CL 时类型不一致。
 * 因此 [AdviceProxy] 仅暴露原始类型签名，由各插件的 incision 在自己 CL 上调用回去。
 */
interface AdviceProxy {

    /** 调用 advice，返回值若非 null 则替换原方法返回值 */
    fun invoke(targetSig: String, self: Any?, args: Array<Any?>): Any?

    /** 排序优先级 — 网关按此降序触发 */
    fun priority(): Int = 0

    /** 注册者标识，用于按插件清理 */
    fun pluginName(): String = ""
}

/**
 * 在本 incision 实例内，把 [AdviceEntry] 包装为可跨 CL 注册的 [AdviceProxy]。
 */
fun AdviceEntry.asProxy(pluginName: String = ""): AdviceProxy = object : AdviceProxy {
    override fun invoke(targetSig: String, self: Any?, args: Array<Any?>): Any? {
        // 直接走本地 dispatcher 的链路 — 由 IncisionGate 调度
        return taboolib.module.incision.runtime.TheatreDispatcher.dispatch(targetSig, self, args)
    }
    override fun priority(): Int = this@asProxy.priority
    override fun pluginName(): String = pluginName
}

/**
 * 跨 CL 字节码 weaver 代理 —— 网关 ensureImplanted 时调用。
 */
interface BytecodeWeaverProxy {

    fun weave(originalBytes: ByteArray, target: MethodCoordinate): ByteArray
}

/**
 * 全局 suture 句柄 — 网关返回给注册方，用于 unregister。
 */
data class GlobalSutureToken(
    val target: MethodCoordinate,
    val proxyRef: Any,
)

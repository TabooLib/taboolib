package taboolib.module.incision.gate

/**
 * IncisionGate 的跨 ClassLoader 契约。
 *
 * 约定：**只用基础类型与 proxy 接口**，不允许出现插件 ClassLoader 中才存在的类型。
 * 真正的实现类由 GateBootstrapper 合成并通过 `Instrumentation.appendToSystemClassLoaderSearch`
 * 推入系统 ClassLoader；若 Instrumentation 不可用，则通过 Bukkit ServicesManager (Exchanges)
 * 共享实例，并由调用方以 invocation handler 代理。
 */
interface IncisionGateApi {

    /** 确保目标方法已物理织入 dispatcher 调用。返回是否是本次新织入。 */
    fun ensureImplanted(targetSignature: String, weaver: BytecodeWeaverProxy): Boolean

    /** 注册一条 advice。返回 token。 */
    fun register(targetSignature: String, advice: AdviceProxy): GlobalSutureToken

    /** 卸载一条 advice。 */
    fun unregister(token: GlobalSutureToken): Boolean

    /** 目标方法被触发时，由字节码调用进来。 */
    fun dispatch(targetSignature: String, self: Any?, args: Array<Any?>): Any?

    /** 当前网关协议版本 */
    fun apiVersion(): Int

    /** 支持的 advice 种类（字符串化） */
    fun supportedAdviceTypes(): Set<String>

    /** 按插件名列出已注册的 advice */
    fun listByPlugin(pluginName: String): List<String>

    /** 强制卸载某个 ClassLoader 下的全部 advice（用于插件卸载） */
    fun healByClassLoader(cl: ClassLoader): Int
}

/** 全局 token — 跨 ClassLoader 反射调用 unregister 时作为句柄 */
interface GlobalSutureToken {
    val targetSignature: String
    val incisionId: String
    val pluginName: String
}

/** 跨 ClassLoader 桥 — 插件的 weaver 通过该接口代理被调用 */
interface BytecodeWeaverProxy {
    /** 输入类 internalName 与原字节码，返回织入后的字节码（或 null 表示不修改） */
    fun transform(internalName: String, bytes: ByteArray): ByteArray?

    fun ownerClass(): String
}

/** 跨 ClassLoader advice 句柄 */
interface AdviceProxy {
    val incisionId: String
    val kind: String // "LEAD" / "TRAIL" / "SPLICE" / ...
    val priority: Int
    val pluginName: String
    fun classLoader(): ClassLoader?
    fun invoke(targetSignature: String, self: Any?, args: Array<Any?>): Any?
}

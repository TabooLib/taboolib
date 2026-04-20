package taboolib.module.incision

import io.izzel.incision.bridge.IncisionBridge
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Awake
import taboolib.module.incision.diagnostic.Checkup
import taboolib.module.incision.diagnostic.ConflictAnalyzer
import taboolib.module.incision.diagnostic.Forensics
import taboolib.module.incision.gate.IncisionGateLocator
import taboolib.module.incision.lifecycle.AutoHealHandler
import taboolib.module.incision.loader.InstrumentationBackend
import taboolib.module.incision.loader.JvmtiBackend
import taboolib.module.incision.loader.PipelineBackend
import taboolib.module.incision.reflex.IncisionReflex
import taboolib.module.incision.remap.RemapRouter
import taboolib.module.incision.remap.TabooLibNmsResolver
import taboolib.module.incision.runtime.SurgeryRegistry
import taboolib.module.incision.runtime.TheatreDispatcher

/**
 * Incision 模块入口。
 *
 * - CONST：模块对象实例化时先准备织入基础设施（resolver / bridge / pipeline / reflex adapter），
 *   随后由 [taboolib.module.incision.loader.SurgeonScanner] 在同一生命周期内完成扫描与物理织入。
 * - INIT / LOAD：不再承担核心织入动作，留给宿主插件自身生命周期逻辑。
 * - ENABLE：接入 IncisionGate（系统级网关），输出 checkup / 冲突分析 / 后端状态。
 *
 * 该入口仅做"调度与汇总"，具体逻辑分散到 SurgeryRegistry / Checkup / Scalpel 等组件。
 */
@Awake
@Inject
object IncisionBootstrap {

    /** 当前 incision API 版本，用于网关版本协商 */
    const val API_VERSION = 1

    init {
        prepareConst()
    }

    private fun prepareConst() {
        // 0. asm-tree 运行时可用性自检（PrimitiveLoader bootstrap 新增）
        probeAsmTree()
        // 1. 尝试接入 TabooLib 的 NMS resolver（若 :module:bukkit-nms 在 classpath）
        TabooLibNmsResolver.installIfAvailable()
        // 2. 安装反射穿透适配器，让 TabooLib reflex 在 invoke 时自动 withoutIncision
        IncisionReflex.installReflexAdapter()
        // 3. 把经 gradle 重定位后真实的 TheatreDispatcher 类名注册进桥（插件 CL 版本）
        try {
            IncisionBridge.registerLocalDispatcher(TheatreDispatcher::class.java)
        } catch (t: Throwable) {
            Forensics.warn("Incision bridge registerLocalDispatcher failed: ${t.message}")
        }
        // 4. 注入 IncisionBridge 到 bootstrap ClassLoader，使跨 CL 目标（NMS、Bukkit API）能解析桥类
        //    必须在任何 installWeaver/retransform 之前完成
        injectBridgeIntoSystemClassLoader()
        Forensics.info("Incision CONST: api=$API_VERSION resolver=${RemapRouter.name()}")
        // 5. 接入 TabooLib NMSProxy 管线末端（PipelineBackend）
        PipelineBackend.installIntoTabooLib()
    }

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        Forensics.info("Incision ENABLE: gate / 诊断收尾开始")
        // 1. 接入 / 创建 IncisionGate
        try {
            val gate = IncisionGateLocator.locateOrCreate(API_VERSION)
            Forensics.info("Incision Gate online: api=${gate.apiVersion()}")
        } catch (t: Throwable) {
            Forensics.warn("Incision Gate 接入失败（将使用本地 dispatcher 兜底）：${t.javaClass.name}: ${t.message}")
            if (Forensics.DEBUG) t.printStackTrace()
        }
        // 2. 输出已注册切术的 startup checkup（此时 CONST 阶段的 @Surgeon 已完成注册）
        Checkup.runStartupCheckup()
        // 3. 跑全量冲突分析
        for (r in ConflictAnalyzer.analyzeAll()) {
            ConflictAnalyzer.emit(r)
        }
        // 4. 输出 retransform 后端状态：优先 Instrumentation，回退到 JVMTI native
        if (InstrumentationBackend.available()) {
            Forensics.info("Instrumentation 后端就绪")
        } else if (JvmtiBackend.available()) {
            Forensics.info("JVMTI native 后端就绪（Instrumentation 不可用）")
        } else {
            Forensics.warn("Instrumentation / JVMTI 均不可用 — 仅 Pipeline / ClassLoaderHook 路径可用")
        }
    }

    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        Forensics.info("Incision DISABLE: 卸载本插件全部 advice")
        // 卸载当前 ClassLoader 下所有 advice
        AutoHealHandler.healByClassLoader(IncisionBootstrap::class.java.classLoader)
        SurgeryRegistry.list().toList().forEach { it.heal() }
        // 解绑本插件在桥上的本地 dispatcher 缓存
        runCatching {
            IncisionBridge.unregisterLocalDispatcher(IncisionBootstrap::class.java.classLoader)
        }
    }

    private fun probeAsmTree() {
        val cl = IncisionBootstrap::class.java.classLoader
        val probes = listOf(
            "org.objectweb.asm.tree.ClassNode",
            "org.objectweb.asm.tree.MethodNode",
            "org.objectweb.asm.tree.InsnList"
        )
        val failed = mutableListOf<String>()
        for (name in probes) {
            try {
                Class.forName(name, false, cl)
            } catch (t: Throwable) {
                failed += "$name(${t.javaClass.simpleName})"
            }
        }
        if (failed.isNotEmpty()) {
            Forensics.warn("asm-tree 不可用 — 缺失: ${failed.joinToString(", ")}")
            return
        }
        if (!Forensics.DEBUG) return
        val asmCoreLoc = runCatching {
            Class.forName("org.objectweb.asm.ClassWriter", false, cl)
                .protectionDomain?.codeSource?.location?.toString()
        }.getOrNull() ?: "?"
        val asmTreeLoc = runCatching {
            Class.forName("org.objectweb.asm.tree.ClassNode", false, cl)
                .protectionDomain?.codeSource?.location?.toString()
        }.getOrNull() ?: "?"
        Forensics.debug("asm-tree probe: loader=${cl.javaClass.name} core=$asmCoreLoc tree=$asmTreeLoc")
        try {
            val bytes = cl.getResourceAsStream("taboolib/module/incision/IncisionBootstrap.class")?.use { it.readBytes() }
            if (bytes != null) {
                val node = org.objectweb.asm.tree.ClassNode()
                org.objectweb.asm.ClassReader(bytes).accept(node, 0)
                Forensics.debug("asm-tree probe OK: name=${node.name} methods=${node.methods.size}")
            }
        } catch (t: Throwable) {
            Forensics.debug("asm-tree probe: tree 读写测试失败 ${t.javaClass.name}: ${t.message}")
        }
    }

    private fun injectBridgeIntoSystemClassLoader() {
        val bridgeClassName = "io.izzel.incision.bridge.IncisionBridge"
        val bridgeInternalName = bridgeClassName.replace('.', '/')
        val sysCL = ClassLoader.getSystemClassLoader()
        // 已存在于 bootstrap 或 system CL — 只需注册 dispatcher
        val existing = try {
            Class.forName(bridgeClassName, true, sysCL)
        } catch (_: Throwable) {
            null
        }
        if (existing != null && existing.classLoader == null) {
            Forensics.info("IncisionBridge 已存在于 bootstrap ClassLoader")
            registerDispatcherOn(existing)
            return
        }
        val resourcePath = "$bridgeInternalName.class"
        val bytes = IncisionBootstrap::class.java.classLoader.getResourceAsStream(resourcePath)?.use { it.readBytes() }
        if (bytes == null) {
            Forensics.warn("IncisionBridge.class 资源未找到: $resourcePath")
            return
        }
        // 路径 1: JVMTI native — defineClass 直接注入 bootstrap CL
        if (JvmtiBackend.available()) {
            val cls = JvmtiBackend.defineClassInClassLoader(null, bridgeClassName, bytes)
            if (cls != null) {
                Forensics.info("IncisionBridge 已注入 bootstrap ClassLoader (JVMTI)")
                registerDispatcherOn(cls)
                return
            }
        }
        // 路径 2: Instrumentation — 打包临时 jar 并 appendToBootstrapClassLoaderSearch
        val inst = InstrumentationBackend.ensure()
        if (inst != null) {
            try {
                val jar = java.io.File.createTempFile("incision-bridge-", ".jar").apply { deleteOnExit() }
                val manifest = java.util.jar.Manifest().apply {
                    mainAttributes[java.util.jar.Attributes.Name.MANIFEST_VERSION] = "1.0"
                }
                java.util.jar.JarOutputStream(java.io.FileOutputStream(jar), manifest).use { out ->
                    out.putNextEntry(java.util.jar.JarEntry("$bridgeInternalName.class"))
                    out.write(bytes)
                    out.closeEntry()
                }
                inst.appendToBootstrapClassLoaderSearch(java.util.jar.JarFile(jar))
                val cls = Class.forName(bridgeClassName, true, sysCL)
                Forensics.info("IncisionBridge 已注入 bootstrap ClassLoader (Instrumentation)")
                registerDispatcherOn(cls)
                return
            } catch (t: Throwable) {
                Forensics.warn("IncisionBridge Instrumentation 注入失败: ${t.javaClass.name}: ${t.message}")
            }
        }
        // 路径 3: fallback — 仅在插件 CL 中，跨 CL 目标无法使用
        Forensics.warn("IncisionBridge 未能注入 bootstrap/system CL — 跨 ClassLoader 目标（NMS、Bukkit API）将不可用")
    }

    private fun registerDispatcherOn(bridgeClass: Class<*>) {
        try {
            val regMethod = bridgeClass.getMethod("registerLocalDispatcher", Class::class.java)
            regMethod.invoke(null, TheatreDispatcher::class.java)
            Forensics.info("IncisionBridge dispatcher 已注册 (CL=${bridgeClass.classLoader ?: "bootstrap"})")
        } catch (t: Throwable) {
            Forensics.warn("IncisionBridge registerLocalDispatcher 失败: ${t.message}")
        }
    }
}

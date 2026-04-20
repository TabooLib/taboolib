package taboolib.module.incision.gate

import taboolib.module.incision.diagnostic.Forensics
import taboolib.module.incision.diagnostic.Trauma
import taboolib.module.incision.loader.InstrumentationBackend
import taboolib.platform.bukkit.Exchanges
import java.io.File
import java.io.FileOutputStream
import java.lang.instrument.Instrumentation
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

/**
 * IncisionGate 引导器 — 决定 gate 实例的承载方式。
 *
 * 优先路径：通过 [Instrumentation.appendToSystemClassLoaderSearch] 把
 * gate jar 推入系统 ClassLoader，使 `taboolib.incision.gate.IncisionGate$V<N>`
 * 成为 JVM 唯一类。
 *
 * Fallback：若 Bukkit `Exchanges` 可用，则共享 [DefaultIncisionGate] 实例；
 * 否则退回进程内单例。
 *
 * 由于跨 ClassLoader 类型不能直接传递，所有调用都通过 `IncisionGateApi` 的
 * **基础类型签名** 进行（避免 IncisionGateApi 自身的类装载冲突，proxy 时通过
 * 反射方法名匹配，不需要类型一致）。
 */
object GateBootstrapper {

    @Volatile
    private var bound: IncisionGateApi? = null

    fun current(): IncisionGateApi? = bound

    fun bootstrap(apiVersion: Int): IncisionGateApi {
        bound?.let { return it }
        synchronized(this) {
            bound?.let { return it }

            val inst = InstrumentationBackend.ensure()
            val gate = if (inst != null) {
                try {
                    bootstrapViaSystemClassLoader(inst, apiVersion)
                } catch (t: Throwable) {
                    Forensics.warn("通过系统 ClassLoader 启动 gate 失败：${t.javaClass.name}: ${t.message}")
                    if (Forensics.DEBUG) t.printStackTrace()
                    bootstrapViaExchanges(apiVersion)
                }
            } else {
                bootstrapViaExchanges(apiVersion)
            }
            bound = gate
            return gate
        }
    }

    /**
     * 把一个最小 gate jar 推入系统 ClassLoader：
     * - jar 内仅包含 `taboolib/incision/gate/IncisionGate$V<N>.class`，由 [DefaultIncisionGate] 字节码复制
     * - 通过 `Instrumentation.appendToSystemClassLoaderSearch(jar)` 后 Class.forName 即可
     */
    private fun bootstrapViaSystemClassLoader(inst: Instrumentation, apiVersion: Int): IncisionGateApi {
        val gateClassName = "taboolib.incision.gate.IncisionGate\$V$apiVersion"
        val internal = gateClassName.replace('.', '/')
        val sysCL = ClassLoader.getSystemClassLoader()

        // 已存在则取 delegate
        val existing = try { Class.forName(gateClassName, true, sysCL) } catch (_: Throwable) { null }
        if (existing != null) {
            val delegate = existing.getMethod("getDelegate").invoke(null)
            if (delegate != null) return wrapAsApi(delegate, apiVersion)
            // delegate 为 null — 被另一个插件创建但尚未 setDelegate；自己设
            val gate = DefaultIncisionGate(apiVersion)
            existing.getMethod("setDelegate", Object::class.java).invoke(null, gate)
            return gate
        }

        // 合成 holder jar 并推入系统 ClassLoader
        val jar = File.createTempFile("incision-gate-v$apiVersion-", ".jar").apply { deleteOnExit() }
        val manifest = Manifest().apply {
            mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
        }
        JarOutputStream(FileOutputStream(jar), manifest).use { out ->
            val bytes = SystemGateClassFactory.makeGateClassBytes(internal, apiVersion)
            out.putNextEntry(JarEntry("$internal.class"))
            out.write(bytes)
            out.closeEntry()
        }
        try {
            inst.appendToSystemClassLoaderSearch(java.util.jar.JarFile(jar))
        } catch (t: Throwable) {
            throw Trauma.Attach.AgentLoadFailed(jar.absolutePath, t)
        }
        val cls = Class.forName(gateClassName, true, sysCL)
        val gate = DefaultIncisionGate(apiVersion)
        cls.getMethod("setDelegate", Object::class.java).invoke(null, gate)
        return gate
    }

    /**
     * fallback 路径：通过 Bukkit Exchanges 共享 gate；若当前平台不可用则退回进程内单例。
     */
    private fun bootstrapViaExchanges(apiVersion: Int): IncisionGateApi {
        val key = "incision.gate.v$apiVersion"
        val instance = try {
            Exchanges.getOrPut(key) { DefaultIncisionGate(apiVersion) }
        } catch (t: Throwable) {
            Forensics.warn("Exchanges 不可用，使用进程内单例：${t.message}")
            DefaultIncisionGate(apiVersion)
        }
        return wrapAsApi(instance, apiVersion)
    }

    /** 把跨 ClassLoader 的对象包成 IncisionGateApi（通过 java.lang.reflect.Proxy） */
    private fun wrapAsApi(instance: Any, apiVersion: Int): IncisionGateApi {
        if (instance is IncisionGateApi) return instance
        val proxy = java.lang.reflect.Proxy.newProxyInstance(
            IncisionGateApi::class.java.classLoader,
            arrayOf(IncisionGateApi::class.java),
        ) { _, method, args ->
            val target = instance.javaClass.methods.firstOrNull { it.name == method.name && it.parameterCount == (args?.size ?: 0) }
                ?: throw RuntimeException("跨 ClassLoader gate 无方法 ${method.name}")
            try {
                target.invoke(instance, *(args ?: emptyArray()))
            } catch (e: java.lang.reflect.InvocationTargetException) {
                throw e.cause ?: e
            }
        }
        return proxy as IncisionGateApi
    }

}

/**
 * 合成系统级 gate holder 类的字节码。
 *
 * 生成的类仅用 JDK 类型，作为系统 ClassLoader 中的"发现锚点"：
 * - INSTANCE：自身实例（供 IncisionGateLocator 发现）
 * - API_VERSION：协议版本常量
 * - delegate：volatile Object，持有真正的 DefaultIncisionGate（位于插件 CL）
 * - setDelegate / getDelegate：访问器
 *
 * 真正的 gate 逻辑由 DefaultIncisionGate 实现，通过 delegate 字段桥接。
 */
internal object SystemGateClassFactory {

    fun makeGateClassBytes(internalName: String, apiVersion: Int): ByteArray {
        val cw = org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_FRAMES or org.objectweb.asm.ClassWriter.COMPUTE_MAXS)
        cw.visit(V1_8, ACC_PUBLIC or ACC_FINAL, internalName, null, "java/lang/Object", emptyArray())

        cw.visitField(ACC_PUBLIC or ACC_STATIC or ACC_FINAL, "INSTANCE", "L$internalName;", null, null).visitEnd()
        cw.visitField(ACC_PUBLIC or ACC_STATIC or ACC_FINAL, "API_VERSION", "I", null, apiVersion).visitEnd()
        cw.visitField(ACC_PRIVATE or ACC_STATIC or ACC_VOLATILE, "delegate", "Ljava/lang/Object;", null, null).visitEnd()

        // private <init>()
        var mv = cw.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null)
        mv.visitCode()
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()

        // <clinit>: INSTANCE = new Self()
        mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null)
        mv.visitCode()
        mv.visitTypeInsn(NEW, internalName)
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKESPECIAL, internalName, "<init>", "()V", false)
        mv.visitFieldInsn(PUTSTATIC, internalName, "INSTANCE", "L$internalName;")
        mv.visitInsn(RETURN)
        mv.visitMaxs(2, 0)
        mv.visitEnd()

        // public static void setDelegate(Object d)
        mv = cw.visitMethod(ACC_PUBLIC or ACC_STATIC, "setDelegate", "(Ljava/lang/Object;)V", null, null)
        mv.visitCode()
        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(PUTSTATIC, internalName, "delegate", "Ljava/lang/Object;")
        mv.visitInsn(RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()

        // public static Object getDelegate()
        mv = cw.visitMethod(ACC_PUBLIC or ACC_STATIC, "getDelegate", "()Ljava/lang/Object;", null, null)
        mv.visitCode()
        mv.visitFieldInsn(GETSTATIC, internalName, "delegate", "Ljava/lang/Object;")
        mv.visitInsn(ARETURN)
        mv.visitMaxs(1, 0)
        mv.visitEnd()
        cw.visitEnd()
        return cw.toByteArray()
    }

    private const val V1_8 = org.objectweb.asm.Opcodes.V1_8
    private const val ACC_PUBLIC = org.objectweb.asm.Opcodes.ACC_PUBLIC
    private const val ACC_PRIVATE = org.objectweb.asm.Opcodes.ACC_PRIVATE
    private const val ACC_STATIC = org.objectweb.asm.Opcodes.ACC_STATIC
    private const val ACC_FINAL = org.objectweb.asm.Opcodes.ACC_FINAL
    private const val ACC_VOLATILE = org.objectweb.asm.Opcodes.ACC_VOLATILE
    private const val ALOAD = org.objectweb.asm.Opcodes.ALOAD
    private const val RETURN = org.objectweb.asm.Opcodes.RETURN
    private const val ARETURN = org.objectweb.asm.Opcodes.ARETURN
    private const val DUP = org.objectweb.asm.Opcodes.DUP
    private const val NEW = org.objectweb.asm.Opcodes.NEW
    private const val INVOKESPECIAL = org.objectweb.asm.Opcodes.INVOKESPECIAL
    private const val PUTSTATIC = org.objectweb.asm.Opcodes.PUTSTATIC
    private const val GETSTATIC = org.objectweb.asm.Opcodes.GETSTATIC
}

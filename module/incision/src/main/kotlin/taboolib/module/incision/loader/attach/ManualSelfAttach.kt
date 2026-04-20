package taboolib.module.incision.loader.attach

import taboolib.module.incision.diagnostic.Trauma
import java.io.File
import java.io.FileOutputStream
import java.lang.instrument.Instrumentation
import java.lang.management.ManagementFactory
import java.net.URLClassLoader
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

/**
 * 手写 self-attach 兜底路径 — 不依赖 byte-buddy-agent。
 *
 * JDK 8：
 * - 通过 `JAVA_HOME/lib/tools.jar` 反射加载 `com.sun.tools.attach.VirtualMachine`
 * - 动态生成一个最小 agent jar（含 Agent-Class / Can-Retransform-Classes manifest）
 * - `VirtualMachine.attach(pid).loadAgent(jar)` → agent `agentmain` 把 Instrumentation 回填到 [captured]
 *
 * JDK 9+：
 * - 直接使用内建 `com.sun.tools.attach`（jdk.attach 模块）
 * - 若进程未启用 `jdk.attach.allowAttachSelf=true`，尝试动态 set 系统属性后再 attach
 */
object ManualSelfAttach {

    /** IncisionSelfAgent.agentmain 回填点 */
    @Volatile
    @JvmStatic
    var captured: Instrumentation? = null

    fun attach(): Instrumentation {
        captured?.let { return it }

        // 启用 self-attach（JDK 9+）
        System.setProperty("jdk.attach.allowAttachSelf", "true")

        val vmClass = loadVirtualMachineClass()
        val pid = currentPid().takeIf { it > 0 }
            ?: throw Trauma.Attach.InstrumentationUnavailable("无法获取当前进程 PID")
        val agentJar = materializeAgentJar()

        val vmInstance = try {
            vmClass.getMethod("attach", String::class.java).invoke(null, pid.toString())
        } catch (t: Throwable) {
            if (isAttachSelfDisabled(t)) throw Trauma.Attach.AttachSelfDisabled()
            if (isReflectionBlocked(t)) throw Trauma.Attach.ReflectionBlocked("jdk.attach/sun.tools.attach", t)
            throw Trauma.Attach.AgentLoadFailed(agentJar.absolutePath, t)
        }
        try {
            vmClass.getMethod("loadAgent", String::class.java, String::class.java)
                .invoke(vmInstance, agentJar.absolutePath, ManualSelfAttach::class.java.name)
        } catch (t: Throwable) {
            throw Trauma.Attach.AgentLoadFailed(agentJar.absolutePath, t)
        } finally {
            try { vmClass.getMethod("detach").invoke(vmInstance) } catch (_: Throwable) {}
        }
        return captured
            ?: throw Trauma.Attach.InstrumentationUnavailable("agent 已加载但未回填 Instrumentation（可能是 IncisionSelfAgent 未被打进 jar）")
    }

    private fun loadVirtualMachineClass(): Class<*> {
        return try {
            Class.forName("com.sun.tools.attach.VirtualMachine")
        } catch (_: ClassNotFoundException) {
            // JDK 8：从 tools.jar 加载
            val javaHome = System.getProperty("java.home")
            val candidates = listOf(
                File(javaHome, "lib/tools.jar"),
                File(File(javaHome).parentFile, "lib/tools.jar"),
                File(javaHome, "../lib/tools.jar"),
            )
            val toolsJar = candidates.firstOrNull { it.exists() }
                ?: throw Trauma.Attach.NoToolsJar(javaHome)
            val cl = URLClassLoader(arrayOf(toolsJar.toURI().toURL()), ClassLoader.getSystemClassLoader())
            Class.forName("com.sun.tools.attach.VirtualMachine", true, cl)
        }
    }

    private fun currentPid(): Long {
        val name = ManagementFactory.getRuntimeMXBean().name
        val at = name.indexOf('@')
        return if (at > 0) name.substring(0, at).toLongOrNull() ?: -1L else -1L
    }

    private fun isAttachSelfDisabled(t: Throwable): Boolean {
        var cur: Throwable? = t
        while (cur != null) {
            val m = cur.message
            if (m != null && (m.contains("allowAttachSelf", true) || m.contains("attach to self", true))) return true
            cur = cur.cause
        }
        return false
    }

    private fun isReflectionBlocked(t: Throwable): Boolean {
        var cur: Throwable? = t
        while (cur != null) {
            val m = cur.message
            if (m != null && (m.contains("module ", true) && m.contains("does not", true))) return true
            cur = cur.cause
        }
        return false
    }

    /** 动态合成最小 agent jar — manifest + IncisionSelfAgent.class */
    private fun materializeAgentJar(): File {
        val tmp = File.createTempFile("incision-self-agent-", ".jar")
        tmp.deleteOnExit()

        // 用运行时实际类名（重定位后可能有前缀）
        val agentClass = IncisionSelfAgent::class.java
        val agentClassName = agentClass.name
        val agentClassPath = agentClassName.replace('.', '/') + ".class"

        val manifest = Manifest().apply {
            mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
            mainAttributes.putValue("Agent-Class", agentClassName)
            mainAttributes.putValue("Premain-Class", agentClassName)
            mainAttributes.putValue("Can-Retransform-Classes", "true")
            mainAttributes.putValue("Can-Redefine-Classes", "true")
        }
        JarOutputStream(FileOutputStream(tmp), manifest).use { jar ->
            val bytes = agentClass.classLoader.getResourceAsStream(agentClassPath)?.readBytes()
                ?: ClassLoader.getSystemResourceAsStream(agentClassPath)?.readBytes()
                ?: throw Trauma.Attach.InstrumentationUnavailable("定位不到 $agentClassPath")
            jar.putNextEntry(JarEntry(agentClassPath))
            jar.write(bytes)
            jar.closeEntry()
        }
        return tmp
    }
}

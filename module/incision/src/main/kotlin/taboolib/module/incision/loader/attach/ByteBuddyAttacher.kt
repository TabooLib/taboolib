package taboolib.module.incision.loader.attach

import java.lang.instrument.Instrumentation

/**
 * byte-buddy-agent 路径 — 若 `net.bytebuddy:byte-buddy-agent` 在 classpath，
 * 反射调用 `ByteBuddyAgent.install()` 拿到 Instrumentation。
 */
object ByteBuddyAttacher {

    fun tryInstall(): Instrumentation? {
        return try {
            val cls = Class.forName("net.bytebuddy.agent.ByteBuddyAgent")
            val method = cls.getMethod("install")
            method.invoke(null) as? Instrumentation
        } catch (_: ClassNotFoundException) {
            null
        } catch (t: Throwable) {
            throw t
        }
    }
}

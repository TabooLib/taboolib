package taboolib.module.incision.cache

import taboolib.module.incision.diagnostic.Forensics
import java.io.File
import java.security.MessageDigest

/**
 * 字节码缓存 — 仿 `taboolib.module.nms.AsmClassTranslation` + `BinaryCache` 的语义。
 *
 * 缓存键 = `srcDigest + adviceConfigDigest + envSignature`，三者任一改变缓存即失效。
 * 缓存目录默认 `<workingDir>/cache/incision/`。
 *
 * 使用约定：
 * - weaver 在第一次织入某个目标类之前先查 [get]
 * - 织入完成后写回 [put]
 * - 命中时直接复用缓存字节，跳过 ASM 流程
 */
object IncisionCache {

    private val md = ThreadLocal.withInitial { MessageDigest.getInstance("SHA-256") }

    /** 缓存目录；可通过系统属性 `incision.cacheDir` 覆盖 */
    @Volatile
    var cacheDir: File = File(System.getProperty("incision.cacheDir") ?: "cache/incision").also {
        runCatching { it.mkdirs() }
    }

    /** 是否启用磁盘缓存；默认开启，调试期可关 */
    @Volatile
    var enabled: Boolean = System.getProperty("incision.cache", "true").toBoolean()

    fun digest(bytes: ByteArray): String {
        val d = md.get()
        d.reset()
        return d.digest(bytes).joinToString("") { "%02x".format(it) }
    }

    fun digest(text: String): String = digest(text.toByteArray(Charsets.UTF_8))

    /** 复合 key */
    fun keyOf(srcDigest: String, adviceConfigDigest: String, envSignature: String): String =
        digest("$srcDigest|$adviceConfigDigest|$envSignature")

    fun get(key: String): ByteArray? {
        if (!enabled) return null
        val f = file(key)
        return try {
            if (f.isFile) f.readBytes() else null
        } catch (t: Throwable) {
            Forensics.warn("IncisionCache get $key failed: ${t.message}")
            null
        }
    }

    fun put(key: String, bytes: ByteArray) {
        if (!enabled) return
        val f = file(key)
        try {
            f.parentFile?.mkdirs()
            f.writeBytes(bytes)
        } catch (t: Throwable) {
            Forensics.warn("IncisionCache put $key failed: ${t.message}")
        }
    }

    fun invalidate(key: String): Boolean {
        val f = file(key)
        return f.isFile && f.delete()
    }

    fun clear() {
        try {
            cacheDir.walkTopDown().filter { it.isFile && it.name.endsWith(".class") }
                .forEach { it.delete() }
        } catch (t: Throwable) {
            Forensics.warn("IncisionCache clear failed: ${t.message}")
        }
    }

    /** 开发模式 dump — 把 before/after 写到 `<dataDir>/.dev/incision/<id>/` */
    fun dump(devDir: File, id: String, before: ByteArray, after: ByteArray) {
        try {
            val dir = File(devDir, ".dev/incision/${safe(id)}").apply { mkdirs() }
            File(dir, "before.class").writeBytes(before)
            File(dir, "after.class").writeBytes(after)
        } catch (t: Throwable) {
            Forensics.warn("IncisionCache dump $id failed: ${t.message}")
        }
    }

    private fun safe(s: String): String = s.replace(Regex("[^A-Za-z0-9._\\-#]"), "_")
    private fun file(key: String): File = File(cacheDir, "${key.take(2)}/$key.class")
}

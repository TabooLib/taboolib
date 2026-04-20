package taboolib.module.incision.loader

import taboolib.module.incision.diagnostic.Forensics
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * JVMTI native backend — retransforms already-loaded classes without
 * -javaagent or -XX:+EnableDynamicAgentLoading.
 *
 * Loads a platform-specific native library bundled in the jar under
 * native/{windows|linux|macos}/{x64|arm64}/incision-jvmti.{dll|so|dylib}
 *
 * The native lib uses JNI_OnLoad + system property "incision.jvmti.class"
 * for dynamic registration, so Gradle Shadow relocation works transparently.
 *
 * The native lib calls back into [onClassFileLoad] for every class that
 * is (re)transformed, routing bytes through the registered transformers.
 */
object JvmtiBackend : Backend {

    private val transformers = ConcurrentHashMap<String, MutableList<(ByteArray) -> ByteArray?>>()

    @Volatile private var loaded = false
    @Volatile private var available = false

    override val name: String = "JVMTI"

    override fun available(): Boolean {
        if (!loaded) tryLoad()
        return available
    }

    override fun addTransformer(className: String, transformer: (ByteArray) -> ByteArray?): Backend.BackendToken {
        val key = className.replace('.', '/')
        transformers.computeIfAbsent(key) { mutableListOf() }.add(transformer)
        return object : Backend.BackendToken {
            override fun remove() { transformers[key]?.remove(transformer) }
        }
    }

    override fun retransform(className: String): Boolean {
        if (!available()) return false
        val cls = findLoadedClass(className)
        if (cls == null) {
            Forensics.debug("JvmtiBackend.retransform: $className not loaded yet, will transform on load")
            return true
        }
        return try {
            val ok = nRetransform(cls)
            Forensics.debug("JvmtiBackend.retransform $className → $ok (loader=${cls.classLoader})")
            ok
        } catch (t: Throwable) {
            Forensics.warn("JvmtiBackend.retransform failed: $className — ${t.message}")
            false
        }
    }

    /** Called from native ClassFileLoadHook for every (re)loaded class. */
    @JvmStatic
    fun onClassFileLoad(loader: ClassLoader?, name: String, bytes: ByteArray): ByteArray? {
        val list = transformers[name]
        if (list == null) return null
        Forensics.debug("JvmtiBackend.onClassFileLoad: $name (${list.size} transformers, ${bytes.size} bytes)")
        var cur = bytes
        var changed = false
        for (t in list) {
            val out = try { t(cur) } catch (e: Throwable) {
                Forensics.error("JvmtiBackend transformer error: $name", e); null
            }
            if (out != null) { cur = out; changed = true }
        }
        Forensics.debug("JvmtiBackend.onClassFileLoad: $name → changed=$changed (${cur.size} bytes)")
        return if (changed) cur else null
    }

    // ----------------------------------------------------------------
    //  Native methods — registered dynamically via JNI_OnLoad
    // ----------------------------------------------------------------

    @JvmStatic private external fun nInit(backendClass: Class<*>): Boolean
    @JvmStatic private external fun nRetransform(target: Class<*>): Boolean
    @JvmStatic private external fun nAvailable(): Boolean
    @JvmStatic external fun nDispose()
    @JvmStatic external fun nDefineClass(loader: ClassLoader?, name: String, bytes: ByteArray): Class<*>?

    @JvmStatic private external fun nCacheOriginal(internalName: String, bytes: ByteArray): Boolean
    @JvmStatic private external fun nGetCachedOriginal(internalName: String): ByteArray?
    @JvmStatic private external fun nExtractClassBytes(target: Class<*>): ByteArray?
    @JvmStatic private external fun nPurgeCache(internalName: String)

    // 通用字段/方法访问器 — JNI 层绕过 Java 访问控制
    @JvmStatic external fun nFieldGet(obj: Any, ownerClass: Class<*>, fieldName: String, fieldDesc: String): Any?
    @JvmStatic external fun nFieldSet(obj: Any, ownerClass: Class<*>, fieldName: String, fieldDesc: String, value: Any?)
    @JvmStatic external fun nStaticFieldGet(ownerClass: Class<*>, fieldName: String, fieldDesc: String): Any?
    @JvmStatic external fun nStaticFieldSet(ownerClass: Class<*>, fieldName: String, fieldDesc: String, value: Any?)
    @JvmStatic external fun nInvokeMethod(obj: Any?, ownerClass: Class<*>, methodName: String, methodDesc: String, args: Array<Any?>?): Any?

    fun defineClassInClassLoader(loader: ClassLoader?, name: String, bytes: ByteArray): Class<*>? {
        if (!available()) return null
        return try {
            val cls = nDefineClass(loader, name.replace('.', '/'), bytes)
            if (cls != null) Forensics.debug("JvmtiBackend.defineClass: $name in ${loader ?: "bootstrap"}")
            cls
        } catch (t: Throwable) {
            Forensics.warn("JvmtiBackend.defineClass failed: $name — ${t.message}")
            null
        }
    }

    fun cacheOriginal(owner: String, bytes: ByteArray): Boolean {
        if (!available()) return false
        return try {
            nCacheOriginal(owner, bytes)
        } catch (t: Throwable) {
            Forensics.warn("nCacheOriginal 失败: ${t.message}")
            false
        }
    }

    fun getCachedOriginal(owner: String): ByteArray? {
        if (!available()) return null
        return try {
            nGetCachedOriginal(owner)
        } catch (t: Throwable) {
            Forensics.warn("nGetCachedOriginal 失败: ${t.message}")
            null
        }
    }

    fun extractClassBytes(target: Class<*>): ByteArray? {
        if (!available()) return null
        return try {
            nExtractClassBytes(target)
        } catch (t: Throwable) {
            Forensics.warn("nExtractClassBytes 失败: ${t.message}")
            null
        }
    }

    fun purgeCache(owner: String) {
        if (!available()) return
        try {
            nPurgeCache(owner)
        } catch (t: Throwable) {
            Forensics.warn("nPurgeCache 失败: ${t.message}")
        }
    }

    // ----------------------------------------------------------------
    //  通用访问器高级 API — 绕过 Java 访问控制
    // ----------------------------------------------------------------

    fun fieldGet(obj: Any, ownerClass: Class<*>, fieldName: String, fieldDesc: String): Any? {
        if (!available()) return null
        return try {
            nFieldGet(obj, ownerClass, fieldName, fieldDesc)
        } catch (t: Throwable) {
            Forensics.warn("nFieldGet 失败: ${ownerClass.name}.$fieldName — ${t.message}")
            null
        }
    }

    fun fieldSet(obj: Any, ownerClass: Class<*>, fieldName: String, fieldDesc: String, value: Any?) {
        if (!available()) return
        try {
            nFieldSet(obj, ownerClass, fieldName, fieldDesc, value)
        } catch (t: Throwable) {
            Forensics.warn("nFieldSet 失败: ${ownerClass.name}.$fieldName — ${t.message}")
        }
    }

    fun staticFieldGet(ownerClass: Class<*>, fieldName: String, fieldDesc: String): Any? {
        if (!available()) return null
        return try {
            nStaticFieldGet(ownerClass, fieldName, fieldDesc)
        } catch (t: Throwable) {
            Forensics.warn("nStaticFieldGet 失败: ${ownerClass.name}.$fieldName — ${t.message}")
            null
        }
    }

    fun staticFieldSet(ownerClass: Class<*>, fieldName: String, fieldDesc: String, value: Any?) {
        if (!available()) return
        try {
            nStaticFieldSet(ownerClass, fieldName, fieldDesc, value)
        } catch (t: Throwable) {
            Forensics.warn("nStaticFieldSet 失败: ${ownerClass.name}.$fieldName — ${t.message}")
        }
    }

    fun invokeMethod(obj: Any?, ownerClass: Class<*>, methodName: String, methodDesc: String, args: Array<Any?>?): Any? {
        if (!available()) return null
        return try {
            nInvokeMethod(obj, ownerClass, methodName, methodDesc, args)
        } catch (t: Throwable) {
            Forensics.warn("nInvokeMethod 失败: ${ownerClass.name}.$methodName — ${t.message}")
            null
        }
    }

    // ----------------------------------------------------------------
    //  Library loading
    // ----------------------------------------------------------------

    @Synchronized
    fun tryLoad(): Boolean {
        if (loaded) return available
        loaded = true
        val lib = extractNativeLib() ?: return false
        return try {
            // Set system property BEFORE System.load so JNI_OnLoad can
            // find the (possibly relocated) class and RegisterNatives.
            System.setProperty("incision.jvmti.class", JvmtiBackend::class.java.name)
            System.load(lib.absolutePath)
            available = nInit(JvmtiBackend::class.java)
            if (available) Forensics.info("JvmtiBackend: JVMTI native loaded from $lib")
            else Forensics.warn("JvmtiBackend: nInit returned false")
            available
        } catch (t: Throwable) {
            Forensics.warn("JvmtiBackend: failed to load native lib — ${t.message}")
            false
        }
    }

    private fun extractNativeLib(): File? {
        val (os, prefix, ext) = platformTriple() ?: return null
        val arch = archName()
        val resource = "native/$os/$arch/${prefix}incision-jvmti.$ext"
        val stream = JvmtiBackend::class.java.classLoader
            .getResourceAsStream(resource) ?: run {
            Forensics.warn("JvmtiBackend: resource not found: $resource")
            return null
        }
        val bytes = stream.use { it.readBytes() }
        val hash = bytes.fold(0L) { acc, b -> acc * 31 + b.toLong() }.toString(16).takeLast(8)
        val dir = File(System.getProperty("java.io.tmpdir"), "incision-native").also { it.mkdirs() }
        val target = File(dir, "${prefix}incision-jvmti-$hash.$ext")
        if (target.exists()) {
            return target
        }
        try {
            target.outputStream().use { output -> output.write(bytes) }
        } catch (e: Throwable) {
            Forensics.warn("JvmtiBackend: extract failed — ${e.message}")
            if (target.exists()) return target
            return null
        }
        return target
    }

    private fun platformTriple(): Triple<String, String, String>? = when {
        System.getProperty("os.name").lowercase().contains("win")   -> Triple("windows", "",    "dll")
        System.getProperty("os.name").lowercase().contains("mac")   -> Triple("macos",   "lib", "dylib")
        System.getProperty("os.name").lowercase().contains("linux") -> Triple("linux",   "lib", "so")
        else -> null
    }

    private fun archName(): String {
        val arch = System.getProperty("os.arch").lowercase()
        return when {
            arch.contains("aarch64") || arch.contains("arm64") -> "arm64"
            else -> "x64"
        }
    }

    private val findLoadedClassMethod: java.lang.reflect.Method? by lazy {
        try {
            ClassLoader::class.java.getDeclaredMethod("findLoadedClass", String::class.java)
                .also { it.isAccessible = true }
        } catch (_: Throwable) { null }
    }

    private fun findLoadedClass(className: String): Class<*>? {
        val m = findLoadedClassMethod ?: return findClass(className)
        for (cl in classLoaders()) {
            try {
                val cls = m.invoke(cl, className) as? Class<*>
                if (cls != null) return cls
            } catch (_: Throwable) {}
        }
        return null
    }

    private fun classLoaders(): List<ClassLoader> = buildList {
        Thread.currentThread().contextClassLoader?.let { add(it) }
        add(JvmtiBackend::class.java.classLoader)
        ClassLoader.getSystemClassLoader()?.let { add(it) }
    }

    private fun findClass(className: String): Class<*>? {
        return try { Class.forName(className, false, Thread.currentThread().contextClassLoader) }
        catch (_: Throwable) {
            try { Class.forName(className, false, JvmtiBackend::class.java.classLoader) }
            catch (_: Throwable) { null }
        }
    }
}

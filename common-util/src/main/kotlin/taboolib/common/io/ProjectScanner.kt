@file:Suppress("UNCHECKED_CAST")

package taboolib.common.io

import org.tabooproject.reflex.ReflexClass
import taboolib.common.ClassAppender
import taboolib.common.PrimitiveIO
import taboolib.common.PrimitiveSettings
import taboolib.common.TabooLib
import taboolib.common.inject.ClassVisitorHandler
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.reflect.ClassMarkers
import taboolib.common.reflect.getAnnotationIfPresent
import java.io.File
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Supplier
import java.util.jar.JarFile
import kotlin.math.abs

/**
 * 是否为开发模式
 */
val isDevelopmentMode: Boolean
    get() = PrimitiveSettings.IS_DEBUG_MODE

/**
 * 当前插件的所有类（在本体中）
 */
val runningClassMapInJar by lazy(LazyThreadSafetyMode.NONE) {
    val map = TabooLib::class.java.protectionDomain.codeSource.location.getClasses()
    // 额外扫描入口
    System.getProperty("taboolib.scan")?.split(',')?.forEach { name ->
        if (name.isEmpty()) return@forEach
        map += Class.forName(name).protectionDomain.codeSource.location.getClasses()
    }
    map
}

/**
 * 当前插件的所有类
 */
val runningClassMap: Map<String, Class<*>>
    get() {
        val map = LinkedHashMap<String, Class<*>>()
        map.putAll(runningClassMapInJar)
        map.putAll(extraLoadedClasses)
        return map
    }

/**
 * 当前插件的所有类（排除第三方库）
 */
val runningClassMapWithoutLibrary: Map<String, Class<*>>
    get() = runningClassMap.filterKeys { !it.contains(".library.") && !it.contains(".libs.") && it.startsWith(groupId) }

/**
 * 当前插件的所有类的集合
 */
val runningClasses: List<Class<*>>
    get() = LinkedList(runningClassMap.values)

/**
 * 当前插件的所有类的集合（排除 TabooLib 第三方库）
 */
val runningClassesWithoutLibrary: List<Class<*>>
    get() = LinkedList(runningClassMapWithoutLibrary.values)

/**
 * 当前插件的所有类（排除匿名类、内部类）
 */
val runningExactClassMap: Map<String, Class<*>>
    get() = runningClassMap.filter { !it.key.contains('$') && it.key.substringAfterLast('$').toIntOrNull() == null }

/**
 * 当前插件的所有类的集合（排除匿名类、内部类）
 */
val runningExactClasses: List<Class<*>>
    get() = LinkedList(runningExactClassMap.values)

/**
 * 当前插件的所有资源文件（在本体中）
 */
val runningResourcesInJar by lazy(LazyThreadSafetyMode.NONE) {
    val map = TabooLib::class.java.protectionDomain.codeSource.location.getResources()
    // 额外扫描入口
    System.getProperty("taboolib.scan")?.split(",")?.forEach { name ->
        if (name.isEmpty()) return@forEach
        map += Class.forName(name).protectionDomain.codeSource.location.getResources()
    }
    map
}

/**
 * 当前插件的所有资源文件
 */
val runningResources: Map<String, ByteArray>
    get() {
        val map = LinkedHashMap<String, ByteArray>()
        map.putAll(runningResourcesInJar)
        map.putAll(extraLoadedResources)
        return map
    }

/**
 * 运行签名
 */
val runningSignature by lazy(LazyThreadSafetyMode.NONE) {
    // 如果在 IDE 中运行
    val main = System.getProperty("taboolib.main")
    if (main != null) {
        val builder = StringBuilder()
        File(Class.forName(main).protectionDomain.codeSource.location.file).walk().forEach { file ->
            if (file.isFile && file.extension == "class") {
                builder.append(file.digest("sha-1"))
            }
        }
        builder.toString().digest("sha-1")
    } else {
        File(TabooLib::class.java.protectionDomain.codeSource.location.file).digest("sha-1")
    }
}

/**
 * 由 ClassAppender 加载的文件
 */
val extraLoadedFiles = CopyOnWriteArrayList<File>()

/**
 * 由 ClassAppender 加载的类
 */
var extraLoadedClasses = ConcurrentHashMap<String, Class<*>>()

/**
 * 由 ClassAppender 加载的资源文件
 */
var extraLoadedResources = ConcurrentHashMap<String, ByteArray>()

/**
 * 标记当前插件可以被注入的有效类
 */
val classMarkers by lazy(LazyThreadSafetyMode.NONE) {
    ClassMarkers(runningSignature)
}

/**
 * 获取 Plugin 实现
 */
fun findPluginImpl(): Plugin? {
    // 从 Jar 中获取类
    val cls = ClassVisitorHandler.getClasses().firstOrNull { Plugin::class.java != it && Plugin::class.java.isAssignableFrom(it) }
    return if (cls != null) {
        try {
            val declaredField = cls.getDeclaredField("INSTANCE")
            declaredField.isAccessible = true
            declaredField.get(null) as Plugin
        } catch (ex: NoSuchFieldException) {
            cls.getDeclaredConstructor().newInstance() as Plugin
        }
    } else null
}

/**
 * 判断平台实现
 */
fun checkPlatform(cls: Class<*>): Boolean {
    val platformSide = cls.getAnnotationIfPresent(PlatformSide::class.java)
    return platformSide == null || platformSide.value.any { i -> i == Platform.CURRENT }
}

/**
 * 取该类在当前项目中被加载的任何实例
 * 例如：@Awake 自唤醒类，或是 Kotlin Companion Object、Kotlin Object 对象
 *
 * @param newInstance 若无任何已加载的实例，是否实例化
 */
fun <T> Class<T>.getInstance(newInstance: Boolean = false): Supplier<T>? {
    // 是否为自唤醒类
    try {
        val awoken = TabooLib.getAwakenedClasses()[name] as? T
        if (awoken != null) {
            return Supplier { awoken }
        }
    } catch (ex: Throwable) {
        when (ex) {
            // 忽略异常
            is ClassNotFoundException, is NoClassDefFoundError -> return null
            // 内部错误
            is InternalError -> {
                PrimitiveIO.println("Failed to get instance: $this")
                ex.printStackTrace()
                return null
            }
        }
    }
    // 反射获取实例字段
    return try {
        // 伴生类
        val instanceObj = if (simpleName == "Companion") {
            ReflexClass.of(classOf(name.substringBeforeLast('$'))).getField("Companion", findToParent = false, remap = false)
        } else {
            ReflexClass.of(this).getField("INSTANCE", findToParent = false, remap = false)
        }
        sup { instanceObj.get() as T }
    } catch (ex: Throwable) {
        when (ex) {
            // 忽略异常
            is ClassNotFoundException, is NoClassDefFoundError, is IllegalAccessError, is IncompatibleClassChangeError -> null
            // 未找到方法
            is NoSuchFieldException -> if (newInstance) sup { getDeclaredConstructor().newInstance() as T } else null
            // 初始化错误 & 内部错误
            is ExceptionInInitializerError, is InternalError -> {
                if (ex.message != "Malformed class name") {
                    PrimitiveIO.println("Failed to get instance: $this")
                    ex.printStackTrace()
                }
                null
            }
            // 其他异常
            else -> throw ex
        }
    }
}

/**
 * 获取 URL 下的所有类
 */
fun URL.getClasses(classLoader: ClassLoader = ClassAppender.getClassLoader()): MutableMap<String, Class<*>> {
    val classes = ConcurrentHashMap<String, Class<*>>()
    val srcFile = try {
        File(toURI())
    } catch (ex: IllegalArgumentException) {
        File((openConnection() as JarURLConnection).jarFileURL.toURI())
    } catch (ex: URISyntaxException) {
        File(path)
    }
    // 是文件
    if (srcFile.isFile) {
        JarFile(srcFile).use { jar ->
            jar.stream().parallel().forEach {
                if (it.name.endsWith(".class")) {
                    val className = it.name.replace('/', '.').substringBeforeLast('.')
                    runCatching { classes[className] = Class.forName(className, false, classLoader) }
                }
            }
        }
    } else {
        srcFile.walkTopDown().filter { it.extension == "class" }.forEach {
            val className = it.path.substringAfter(srcFile.path).drop(1).replace('/', '.').substringBeforeLast(".class")
            runCatching { classes[className] = Class.forName(className, false, classLoader) }
        }
    }
    return classes
}

/**
 * 获取 URL 下的所有文件
 */
fun URL.getResources(): MutableMap<String, ByteArray> {
    val resources = LinkedHashMap<String, ByteArray>()
    val srcFile = try {
        File(toURI())
    } catch (ex: IllegalArgumentException) {
        File((openConnection() as JarURLConnection).jarFileURL.toURI())
    } catch (ex: URISyntaxException) {
        File(path)
    }
    val jarFile = JarFile(srcFile)
    jarFile.stream().filter { !it.name.endsWith(".class") && !it.isDirectory }.forEach {
        resources[it.name] = jarFile.getInputStream(jarFile.getJarEntry(it.name)).readBytes()
    }
    return resources
}

private fun classOf(name: String): Class<*> {
    return Class.forName(name, false, ClassAppender.getClassLoader())
}

private fun <T> sup(supplier: () -> T): Supplier<T> {
    return object : Supplier<T> {

        val value by lazy(LazyThreadSafetyMode.NONE) { supplier() }

        override fun get(): T {
            return value
        }
    }
}

/**
 * 初始化函数
 */
private fun init() {
    ClassAppender.registerCallback { loader, file, isExternal ->
        // 只有内部库会被收录
        if (!isExternal) {
            extraLoadedFiles += file
            extraLoadedClasses += file.toURI().toURL().getClasses(loader)
            extraLoadedResources += file.toURI().toURL().getResources()
        }
    }
}
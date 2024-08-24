package taboolib.common.io

import org.tabooproject.reflex.LazyClass
import org.tabooproject.reflex.ReflexClass
import taboolib.common.ClassAppender
import taboolib.common.PrimitiveIO
import taboolib.common.TabooLib
import java.io.File
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.jar.JarFile

/**
 * 当前插件的所有类（在本体中）
 */
val runningClassMapInJar by lazy(LazyThreadSafetyMode.NONE) {
    val time = System.currentTimeMillis()
    val map = TabooLib::class.java.protectionDomain.codeSource.location.getClasses()
    // 额外扫描入口
    System.getProperty("taboolib.scan")?.split(',')?.forEach { name ->
        if (name.isEmpty()) return@forEach
        map += Class.forName(name).protectionDomain.codeSource.location.getClasses()
    }
    // 扫描额外主类
    val main = System.getProperty("taboolib.main")
    if (main != null) {
        map += Class.forName(main).protectionDomain.codeSource.location.getClasses()
    }
    PrimitiveIO.debug("Loaded %s classes in (%sms).", map.size, System.currentTimeMillis() - time)
    map
}

/**
 * 当前插件的所有类
 */
val runningClassMap: Map<String, ReflexClass>
    get() {
        val map = LinkedHashMap(runningClassMapInJar)
        map.putAll(extraLoadedClasses)
        return map
    }

/**
 * 当前插件的所有类（排除第三方库）
 */
val runningClassMapWithoutLibrary: Map<String, ReflexClass>
    get() = runningClassMap.filterKeys { !it.contains(".library.") && !it.contains(".libs.") && it.startsWith(groupId) }

/**
 * 当前插件的所有类的集合
 */
val runningClasses: List<ReflexClass>
    get() = LinkedList(runningClassMap.values)

/**
 * 当前插件的所有类的集合（排除 TabooLib 第三方库）
 */
val runningClassesWithoutLibrary: List<ReflexClass>
    get() = LinkedList(runningClassMapWithoutLibrary.values)

/**
 * 当前插件的所有类（排除匿名类、内部类）
 */
val runningExactClassMap: Map<String, ReflexClass>
    get() = runningClassMap.filter { !it.key.contains('$') && it.key.substringAfterLast('$').toIntOrNull() == null }

/**
 * 当前插件的所有类的集合（排除匿名类、内部类）
 */
val runningExactClasses: List<ReflexClass>
    get() = LinkedList(runningExactClassMap.values)

/**
 * 当前插件的所有资源文件（在本体中）
 */
val runningResourcesInJar by lazy(LazyThreadSafetyMode.NONE) {
    val time = System.currentTimeMillis()
    val map = TabooLib::class.java.protectionDomain.codeSource.location.getResources()
    // 额外扫描入口
    System.getProperty("taboolib.scan")?.split(",")?.forEach { name ->
        if (name.isEmpty()) return@forEach
        PrimitiveIO.println("Scanning $name")
        map += Class.forName(name).protectionDomain.codeSource.location.getResources()
    }
    // 扫描额外主类
    val main = System.getProperty("taboolib.main")
    if (main != null) {
        map += Class.forName(main).protectionDomain.codeSource.location.getResources()
    }
    PrimitiveIO.debug("Loaded %s resources in (%sms).", map.size, System.currentTimeMillis() - time)
    map
}

/**
 * 当前插件的所有资源文件
 */
val runningResources: Map<String, ByteArray>
    get() {
        val map = LinkedHashMap(runningResourcesInJar)
        map.putAll(extraLoadedResources)
        return map
    }

/**
 * 由 ClassAppender 加载的文件
 */
val extraLoadedFiles = CopyOnWriteArrayList<File>()

/**
 * 由 ClassAppender 加载的类
 */
var extraLoadedClasses = ConcurrentHashMap<String, ReflexClass>()

/**
 * 由 ClassAppender 加载的资源文件
 */
var extraLoadedResources = ConcurrentHashMap<String, ByteArray>()

/**
 * 获取 URL 下的所有类
 */
fun URL.getClasses(classLoader: ClassLoader = ClassAppender.getClassLoader()): MutableMap<String, ReflexClass> {
    val classes = ConcurrentHashMap<String, ReflexClass>()
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
            jar.stream()
                .parallel()
                .filter { it.name.endsWith(".class") }
                .forEach {
                    val className = it.name.replace('/', '.').substringBeforeLast('.')
                    val lc = LazyClass.of(className) { Class.forName(className, false, classLoader) }
                    classes[className] = ReflexClass.of(lc, jar.getInputStream(it))
                }
        }
    } else {
        srcFile.walk().filter { it.extension == "class" }.forEach {
            val className = it.path.substringAfter(srcFile.path).drop(1).replace('/', '.').substringBeforeLast(".class")
            val lc = LazyClass.of(className) { Class.forName(className, false, classLoader) }
            classes[className] = ReflexClass.of(lc, it.inputStream())
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
    jarFile.stream().parallel().filter { !it.name.endsWith(".class") && !it.isDirectory }.forEach {
        resources[it.name] = jarFile.getInputStream(jarFile.getJarEntry(it.name)).readBytes()
    }
    return resources
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
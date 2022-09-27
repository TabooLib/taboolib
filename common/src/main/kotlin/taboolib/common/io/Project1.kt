@file:Suppress("UNCHECKED_CAST")

package taboolib.common.io

import org.tabooproject.reflex.ReflexClass
import taboolib.common.TabooLibCommon
import taboolib.common.inject.VisitorHandler
import taboolib.common.platform.PlatformFactory
import taboolib.common.util.lazySupplier
import taboolib.common.util.unsafeLazy
import java.io.File
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.*
import java.util.function.Supplier
import java.util.jar.JarFile

/**
 * 当前插件的所有类
 */
val runningClassMap by unsafeLazy { TabooLibCommon::class.java.protectionDomain.codeSource.location.getClasses() }

val runningClassMapWithoutLibrary by unsafeLazy { runningClassMap.filterKeys { !it.contains("$taboolibId.library") } }

val runningClasses by unsafeLazy { LinkedList(runningClassMap.values) }

val runningClassesWithoutLibrary by unsafeLazy { LinkedList(runningClassMapWithoutLibrary.values) }

val runningExactClassMap by unsafeLazy { runningClassMap.filter { !it.key.contains('$') && it.key.substringAfterLast('$').toIntOrNull() == null } }

val runningExactClasses by unsafeLazy { LinkedList(runningExactClassMap.values) }

/**
 * 取该类在当前项目中被加载的任何实例
 * 例如：@Awake 自唤醒类，或是 Kotlin Companion Object、Kotlin Object 对象
 * @param newInstance 若无任何已加载的实例，是否实例化
 */
fun <T> Class<T>.getInstance(newInstance: Boolean = false): Supplier<T>? {
    try {
        val awoken = PlatformFactory.awokenMap[name] as? T
        if (awoken != null) {
            return Supplier { awoken }
        }
    } catch (ex: ClassNotFoundException) {
        return null
    } catch (ex: NoClassDefFoundError) {
        return null
    } catch (ex: InternalError) {
        println(this)
        ex.printStackTrace()
        return null
    }
    return try {
        val field = if (simpleName == "Companion") {
            val companion = Class.forName(name.substringBeforeLast('$'), false, TabooLibCommon::class.java.classLoader)
            ReflexClass.of(companion).getField("Companion", findToParent = false, remap = false)
        } else {
            ReflexClass.of(this).getField("INSTANCE", findToParent = false, remap = false)
        }
        lazySupplier { field.get() as T }
    } catch (ex: NoSuchFieldException) {
        if (newInstance) lazySupplier { getDeclaredConstructor().newInstance() as T } else null
    } catch (ex: NoClassDefFoundError) {
        null
    } catch (ex: ClassNotFoundException) {
        null
    } catch (ex: IllegalAccessError) {
        null
    } catch (ex: IncompatibleClassChangeError) {
        null
    } catch (ex: ExceptionInInitializerError) {
        println(this)
        ex.printStackTrace()
        null
    } catch (ex: InternalError) {
        // 非常奇怪的错误
        if (ex.message != "Malformed class name") {
            println(this)
            ex.printStackTrace()
        }
        null
    }
}

fun <T> Class<T>.inject() {
    return VisitorHandler.injectAll(this)
}

fun <T> Class<T>.findImplementation(): T? {
    return runningClasses.firstOrNull { isAssignableFrom(it) && it != this && PlatformFactory.checkPlatform(it) }?.getInstance(true)?.get() as? T
}

fun URL.getClasses(): Map<String, Class<*>> {
    val classes = LinkedHashMap<String, Class<*>>()
    val src = ArrayList<File>()
    val srcFile = try {
        File(toURI())
    } catch (ex: IllegalArgumentException) {
        File((openConnection() as JarURLConnection).jarFileURL.toURI())
    } catch (ex: URISyntaxException) {
        File(path)
    }
    val springBootWar = srcFile.parentFile.name == "lib" && srcFile.parentFile.parentFile.name == "WEB-INF"
    if (springBootWar) {
        // include taboolib modules
        srcFile.parentFile.listFiles()?.forEach {
            if (it.name.startsWith("taboolib")) {
                src += it
            }
        }
        fun include(file: File, path: String = "") {
            if (file.isDirectory) {
                file.listFiles()?.forEach { sub -> include(sub, "$path${file.name}.") }
            } else {
                val className = "$path${file.nameWithoutExtension}"
                kotlin.runCatching {
                    classes[className] = Class.forName(className, false, TabooLibCommon::class.java.classLoader)
                }
            }
        }
        File(srcFile.parentFile.parentFile, "classes").listFiles()?.forEach {
            include(it)
        }
    } else {
        src += srcFile
    }
    src.forEach { s ->
        JarFile(s).stream().filter { it.name.endsWith(".class") }.forEach {
            val className = it.name.replace('/', '.').substring(0, it.name.length - 6)
            kotlin.runCatching {
                classes[className] = Class.forName(className, false, TabooLibCommon::class.java.classLoader)
            }
        }
    }
    return classes
}
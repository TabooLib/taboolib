@file:Suppress("UNCHECKED_CAST")

package taboolib.common.io

import taboolib.common.TabooLibCommon
import taboolib.common.inject.RuntimeInjector
import taboolib.common.platform.PlatformFactory
import taboolib.common.util.lazySupplier
import java.io.File
import java.net.JarURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.util.function.Supplier
import java.util.jar.JarFile

/**
 * 当前插件的所有类
 */
val runningClasses by lazy { TabooLibCommon::class.java.protectionDomain.codeSource.location.getClasses() }

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
            Class.forName(name.substringBeforeLast('$')).getDeclaredField("Companion")
        } else {
            getDeclaredField("INSTANCE")
        }
        field.isAccessible = true
        lazySupplier { field.get(null) as T }
    } catch (ex: NoClassDefFoundError) {
        null
    } catch (ex: NoSuchFieldException) {
        if (newInstance) lazySupplier { getDeclaredConstructor().newInstance() as T } else null
    } catch (ex: ClassNotFoundException) {
        null
    } catch (ex: ExceptionInInitializerError) {
        println(this)
        ex.printStackTrace()
        null
    }
}

fun <T> Class<T>.inject() {
    return RuntimeInjector.injectAll(this)
}

fun <T> Class<T>.findImplementation(): T? {
    return runningClasses.firstOrNull { isAssignableFrom(it) && it != this && PlatformFactory.checkPlatform(it) }?.getInstance(true)?.get() as? T
}

fun URL.getClasses(): List<Class<*>> {
    val src = try {
        File(toURI())
    } catch (ex: IllegalArgumentException) {
        File((openConnection() as JarURLConnection).jarFileURL.toURI())
    } catch (ex: URISyntaxException) {
        File(path)
    }
    val classes = ArrayList<Class<*>>()
    JarFile(src).stream().filter { it.name.endsWith(".class") }.forEach {
        try {
            classes.add(Class.forName(it.name.replace('/', '.').substring(0, it.name.length - 6), false, TabooLibCommon::class.java.classLoader))
        } catch (ex: Throwable) {
        }
    }
    return classes
}
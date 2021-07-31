@file:Suppress("UNCHECKED_CAST", "NO_REFLECTION_IN_CLASS_PATH")

package taboolib.common.io

import taboolib.common.TabooLibCommon
import taboolib.common.inject.RuntimeInjector
import taboolib.common.platform.PlatformFactory
import java.io.*
import java.math.BigInteger
import java.net.URISyntaxException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.function.Supplier
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

val runningClasses = TabooLibCommon::class.java.protectionDomain.codeSource.location.getClasses()

fun <T> Class<T>.getInstance(newInstance: Boolean = false): Supplier<T>? {
    try {
        val awoken = PlatformFactory.getAPI<T>(simpleName)
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
        Supplier { field.get(null) as T }
    } catch (ex: NoSuchFieldException) {
        if (newInstance) Supplier { getDeclaredConstructor().newInstance() as T } else null
    } catch (ex: ClassNotFoundException) {
        null
    } catch (ex: NoClassDefFoundError) {
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
    return runningClasses.firstOrNull { isAssignableFrom(it) && it != this }?.getInstance(true)?.get() as? T
}

fun URL.getClasses(): List<Class<*>> {
    val src = try {
        File(toURI())
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

fun String.digest(algorithm: String): String {
    val digest = MessageDigest.getInstance(algorithm)
    digest.update(toByteArray(StandardCharsets.UTF_8))
    return BigInteger(1, digest.digest()).toString(16)
}

fun File.digest(algorithm: String): String {
    return FileInputStream(this).use {
        val digest = MessageDigest.getInstance(algorithm)
        val buffer = ByteArray(1024)
        var length: Int
        while (it.read(buffer, 0, 1024).also { i -> length = i } != -1) {
            digest.update(buffer, 0, length)
        }
        BigInteger(1, digest.digest()).toString(16)
    }
}

fun File.deepCopyTo(target: File) {
    if (!target.exists()) {
        if (isDirectory) {
            target.mkdirs()
        } else {
            target.createNewFile()
        }
    }
    if (isDirectory) {
        listFiles()?.forEach { it.deepCopyTo(File(target, it.name)) }
    } else {
        copyTo(target)
    }
}

fun File.deepDelete() {
    if (exists()) {
        if (isDirectory) {
            listFiles()?.forEach { it.deepDelete() }
        } else {
            delete()
        }
    }
}

fun File.toZip(target: File) {
    FileOutputStream(target).use { fileOutputStream -> ZipOutputStream(fileOutputStream).use { it -> it.toZip(this, "") } }
}

fun File.toZipSkipDirectory(target: File) {
    FileOutputStream(target).use { fileOutputStream ->
        ZipOutputStream(fileOutputStream).use { zipOutputStream ->
            if (isDirectory) {
                listFiles()?.forEach { zipOutputStream.toZip(it, "") }
            } else {
                zipOutputStream.toZip(this, "")
            }
        }
    }
}

fun ZipOutputStream.toZip(file: File, path: String) {
    if (file.isDirectory) {
        file.listFiles()?.forEach { toZip(it, path + file.name + "/") }
    } else {
        FileInputStream(file).use {
            putNextEntry(ZipEntry(path + file.name))
            write(it.readBytes())
            flush()
            closeEntry()
        }
    }
}

fun File.fromZip(target: File) {
    fromZip(target.path)
}

fun File.fromZip(destDirPath: String) {
    ZipFile(this).use { zipFile ->
        zipFile.stream().forEach { entry ->
            if (entry.isDirectory) {
                File(destDirPath + "/" + entry.name).mkdirs()
            } else {
                zipFile.getInputStream(entry).use {
                    File(destDirPath + "/" + entry.name).writeBytes(it.readBytes())
                }
            }
        }
    }
}

fun Serializable.serialize(builder: ObjectOutputStream.() -> Unit = {}): ByteArray {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
            builder(objectOutputStream)
            objectOutputStream.writeObject(this)
            objectOutputStream.flush()
        }
        return byteArrayOutputStream.toByteArray()
    }
}

fun <T> ByteArray.deserialize(reader: ObjectInputStream.() -> Unit = {}): T {
    ByteArrayInputStream(this).use { byteArrayInputStream ->
        ObjectInputStream(byteArrayInputStream).use { objectInputStream ->
            reader(objectInputStream)
            return objectInputStream.readObject() as T
        }
    }
}

fun newFile(file: File, path: String, create: Boolean = false): File {
    return newFile(File(file, path), create)
}

fun newFile(path: String, create: Boolean = false): File {
    return newFile(File(path), create)
}

fun newFile(file: File, create: Boolean = false): File {
    if (!file.parentFile.exists()) {
        file.parentFile.mkdirs()
    }
    if (!file.exists() && create) {
        file.createNewFile()
    }
    return file
}
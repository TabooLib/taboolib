@file:Isolated

package taboolib.common.io

import taboolib.common.Isolated
import taboolib.common.util.using
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * 压缩文件
 * @param target 压缩后的文件
 * @param skipParent 是否跳过该文件，从子文件开始压缩
 */
fun File.zip(target: File, skipParent: Boolean = false) {
    if (skipParent) {
        if (!isDirectory) error("is not directory")
        using {
            val fileStream = FileOutputStream(target).cage()
            val zipStream = ZipOutputStream(fileStream).cage()
            this@zip.listFiles()?.forEach { zipStream.putFile(it, "") }
        }
    } else {
        using {
            val fileStream = FileOutputStream(target).cage()
            val zipStream = ZipOutputStream(fileStream).cage()
            zipStream.putFile(this@zip, "")
        }
    }
}

/**
 * 解压文件
 * @param target 解压后的文件
 */
fun File.unzip(target: File) {
    unzip(target.path)
}

/**
 * 解压文件
 * @param destDirPath 解压后的文件路径
 */
@Suppress("NestedBlockDepth")
fun File.unzip(destDirPath: String) {
    using {
        val zip = ZipFile(this@unzip).cage()
        val stream = zip.stream().parallel()

        stream
            .filter { it.isDirectory }
            .forEach { File(destDirPath + "/" + it.name).mkdirs() }

        stream
            .filter { !it.isDirectory }
            .forEach { entry ->
                val inputStream = zip.getInputStream(entry).cage()
                File(destDirPath + "/" + entry.name).writeBytes(inputStream.readBytes())
            }
    }
}

fun ZipOutputStream.putFile(file: File, path: String) {
    if (file.isDirectory) {
        file.listFiles()?.forEach { putFile(it, path + file.name + "/") }
    } else {
        FileInputStream(file).use {
            putNextEntry(ZipEntry(path + file.name))
            write(it.readBytes())
            flush()
            closeEntry()
        }
    }
}

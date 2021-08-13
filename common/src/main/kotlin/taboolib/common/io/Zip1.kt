@file:Isolated
package taboolib.common.io

import taboolib.common.Isolated
import java.io.*
import java.util.zip.*

fun File.zip(target: File) {
    FileOutputStream(target).use { fileOutputStream -> ZipOutputStream(fileOutputStream).use { it.putFile(this, "") } }
}

fun File.unzip(target: File) {
    unzip(target.path)
}

fun File.unzip(destDirPath: String) {
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
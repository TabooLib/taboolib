package taboolib.common.io

import java.io.File

fun newFile(file: File, path: String, create: Boolean = true): File {
    return newFile(File(file, path), create)
}

fun newFile(path: String, create: Boolean = true): File {
    return newFile(File(path), create)
}

fun newFile(file: File, create: Boolean = true): File {
    if (!file.parentFile.exists()) {
        file.parentFile.mkdirs()
    }
    if (!file.exists() && create) {
        file.createNewFile()
    }
    return file
}
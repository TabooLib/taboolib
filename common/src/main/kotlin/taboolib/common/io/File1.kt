package taboolib.common.io

import java.io.File

fun newFile(file: File, path: String, create: Boolean = true, folder: Boolean = false): File {
    return newFile(File(file, path), create, folder)
}

fun newFile(path: String, create: Boolean = true, folder: Boolean = false): File {
    return newFile(File(path), create, folder)
}

fun newFile(file: File, create: Boolean = true, folder: Boolean = false): File {
    if (!file.parentFile.exists()) {
        file.parentFile.mkdirs()
    }
    if (!file.exists() && create) {
        if (folder) {
            file.mkdirs()
        } else {
            file.createNewFile()
        }
    }
    return file
}
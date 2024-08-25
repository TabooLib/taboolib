package taboolib.common.io

import java.io.File

fun File.notfound(): Boolean {
    return !exists()
}
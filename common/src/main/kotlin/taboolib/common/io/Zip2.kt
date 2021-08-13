@file:Isolated
package taboolib.common.io

import taboolib.common.Isolated
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

fun ByteArray.zip(): ByteArray {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        GZIPOutputStream(byteArrayOutputStream).use { gzipOutputStream ->
            gzipOutputStream.write(this)
            gzipOutputStream.flush()
        }
        return byteArrayOutputStream.toByteArray()
    }
}

fun ByteArray.unzip(): ByteArray {
    ByteArrayInputStream(this).use { byteArrayOutputStream ->
        GZIPInputStream(byteArrayOutputStream).use { gzipInputStream ->
            return gzipInputStream.readBytes()
        }
    }
}
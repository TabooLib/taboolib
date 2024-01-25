package taboolib.common.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * 使用 GZIP 算法压缩字节
 */
fun ByteArray.zip(): ByteArray {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        GZIPOutputStream(byteArrayOutputStream).use { gzipOutputStream ->
            gzipOutputStream.write(this)
            gzipOutputStream.flush()
        }
        return byteArrayOutputStream.toByteArray()
    }
}

/**
 * 使用 GZIP 算法解压字节
 */
fun ByteArray.unzip(): ByteArray {
    ByteArrayInputStream(this).use { byteArrayOutputStream ->
        GZIPInputStream(byteArrayOutputStream).use { gzipInputStream ->
            return gzipInputStream.readBytes()
        }
    }
}
@file:Isolated
package taboolib.common.io

import taboolib.common.Isolated
import taboolib.common.util.using
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * 使用 GZIP 算法压缩字节
 */
fun ByteArray.zip(): ByteArray = using {
    val byteStream = ByteArrayOutputStream().autoClose()
    val gzipStream = GZIPOutputStream(byteStream).autoClose()

    gzipStream.write(this@zip)
    gzipStream.flush()

    return byteStream.toByteArray()
}

/**
 * 使用 GZIP 算法解压字节
 */
fun ByteArray.unzip(): ByteArray = using {
    val byteStream = ByteArrayInputStream(this@unzip).autoClose()
    val gzipStream = GZIPInputStream(byteStream).autoClose()

    return gzipStream.readBytes()
}

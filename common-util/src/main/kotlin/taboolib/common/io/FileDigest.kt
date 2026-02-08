package taboolib.common.io

import java.io.File
import java.math.BigInteger
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption
import java.security.MessageDigest

/**
 * 取字符串的数字签名
 *
 * @param algorithm 算法类型（可使用：md5, sha-1, sha-256 等）
 * @return 数字签名
 */
fun String.digest(algorithm: String = "sha-1"): String {
    return toByteArray(StandardCharsets.UTF_8).digest(algorithm)
}

/**
 * 取 ByteArray 的数字签名
 * @param algorithm 算法类型（可使用：md5, sha-1, sha-256 等）
 * @return 数字签名
 */
fun ByteArray.digest(algorithm: String = "sha-1"): String {
    val digest = MessageDigest.getInstance(algorithm)
    digest.update(this)
    return BigInteger(1, digest.digest()).toString(16)
}

/**
 * 取文件的数字签名
 *
 * @param algorithm 算法类型（可使用：md5, sha-1, sha-256 等）
 * @return 数字签名
 */
fun File.digest(algorithm: String = "sha-1"): String {
    return FileChannel.open(toPath(), StandardOpenOption.READ).use { channel ->
        val digest = MessageDigest.getInstance(algorithm)
        val buffer = ByteBuffer.allocateDirect(1024 * 1024)
        while (channel.read(buffer) != -1) {
            // 显式转换为 Buffer 类型以确保 Java 8 兼容性
            (buffer as Buffer).flip()
            digest.update(buffer)
            (buffer as Buffer).clear()
        }
        BigInteger(1, digest.digest()).toString(16)
    }
}
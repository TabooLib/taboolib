package taboolib.common.io

import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * 取字符串的数字签名
 *
 * @param algorithm 算法类型（可使用：md5, sha-1, sha-256 等）
 * @return 数字签名
 */
fun String.digest(algorithm: String): String {
    val digest = MessageDigest.getInstance(algorithm)
    digest.update(toByteArray(StandardCharsets.UTF_8))
    return BigInteger(1, digest.digest()).toString(16)
}

/**
 * 取文件的数字签名
 *
 * @param algorithm 算法类型（可使用：md5, sha-1, sha-256 等）
 * @return 数字签名
 */
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
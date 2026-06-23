package taboolib.expansion.migration

import java.security.MessageDigest

/**
 * 单个 SQL 迁移脚本。
 *
 * @property version 迁移版本号
 * @property description 文件名中的描述部分
 * @property script 文件名
 * @property checksum SQL 文件原始内容的 SHA-256
 * @property sql SQL 文件正文
 */
data class MigrationScript(
    val version: Int,
    val description: String,
    val script: String,
    val checksum: String,
    val sql: String,
) {

    companion object {

        /**
         * 迁移脚本命名规则：`V版本__说明.sql`。
         */
        val NAME_PATTERN = Regex("^V(\\d+)__(.+)\\.sql$")

        /**
         * 根据资源路径和文件内容创建脚本模型。
         *
         * @param resourcePath classpath 资源路径
         * @param bytes SQL 文件原始字节
         * @return 迁移脚本模型
         */
        fun fromResource(resourcePath: String, bytes: ByteArray): MigrationScript {
            val fileName = resourcePath.substringAfterLast('/')
            val match = NAME_PATTERN.matchEntire(fileName) ?: throw MigrationException("Invalid migration script name: $fileName")
            return MigrationScript(
                version = match.groupValues[1].toInt(),
                description = match.groupValues[2],
                script = fileName,
                checksum = sha256(bytes),
                sql = bytes.toString(Charsets.UTF_8),
            )
        }

        /**
         * 计算 SQL 文件校验值。
         *
         * @param bytes 文件原始字节
         * @return 十六进制 SHA-256
         */
        fun sha256(bytes: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}

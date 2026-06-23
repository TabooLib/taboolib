package taboolib.expansion.migration

import java.io.File
import java.net.JarURLConnection
import java.util.jar.JarFile

/**
 * 从 classpath 目录中扫描 SQL 迁移脚本。
 *
 * @property files SQL 文件迁移配置
 */
class MigrationResourceScanner(
    val files: MigrationFiles,
) {

    /**
     * 读取、解析并按版本排序迁移脚本。
     *
     * @return 迁移脚本列表
     */
    fun load(): List<MigrationScript> {
        val scripts = collectResourcePaths().map { resourcePath ->
            val bytes = files.classLoader.getResourceAsStream(resourcePath)?.use { it.readBytes() }
                ?: throw MigrationException("Migration script not found: $resourcePath")
            MigrationScript.fromResource(resourcePath, bytes)
        }.sortedBy { it.version }

        val duplicate = scripts.groupBy { it.version }.filterValues { it.size > 1 }.keys.firstOrNull()
        if (duplicate != null) {
            throw MigrationException("Duplicate migration version: V$duplicate")
        }
        return scripts
    }

    /**
     * 收集迁移目录下的脚本资源路径。
     * 仅读取目录第一层，避免跨模块误扫子目录。
     *
     * @return classpath 资源路径列表
     */
    fun collectResourcePaths(): List<String> {
        val normalizedPath = files.path.trim('/').replace('\\', '/')
        val resources = linkedSetOf<String>()
        val urls = files.classLoader.getResources(normalizedPath)
        while (urls.hasMoreElements()) {
            val url = urls.nextElement()
            when (url.protocol) {
                "file" -> collectFileResources(File(url.toURI()), normalizedPath, resources)
                "jar" -> collectJarResources(url.openConnection() as JarURLConnection, normalizedPath, resources)
            }
        }
        return resources.filter { it.substringAfterLast('/').matches(MigrationScript.NAME_PATTERN) }.sorted()
    }

    /**
     * 从文件系统 classpath 目录收集脚本。
     *
     * @param directory classpath 对应目录
     * @param normalizedPath 标准化后的迁移目录
     * @param resources 收集到的资源路径
     */
    fun collectFileResources(directory: File, normalizedPath: String, resources: MutableSet<String>) {
        val files = directory.listFiles() ?: return
        for (file in files) {
            if (file.isFile) {
                resources += "$normalizedPath/${file.name}"
            }
        }
    }

    /**
     * 从 Jar classpath 目录收集脚本。
     *
     * @param connection Jar 资源连接
     * @param normalizedPath 标准化后的迁移目录
     * @param resources 收集到的资源路径
     */
    fun collectJarResources(connection: JarURLConnection, normalizedPath: String, resources: MutableSet<String>) {
        val prefix = "$normalizedPath/"
        val jarFile: JarFile = connection.jarFile
        val entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (!entry.isDirectory && entry.name.startsWith(prefix) && '/' !in entry.name.removePrefix(prefix)) {
                resources += entry.name
            }
        }
    }
}

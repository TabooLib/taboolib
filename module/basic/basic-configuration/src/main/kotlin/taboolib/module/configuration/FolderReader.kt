package taboolib.module.configuration

import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFolder
import taboolib.module.configuration.Configuration.Companion.getTypeFromExtension
import taboolib.module.configuration.Configuration.Companion.loadFromFile
import java.io.File

/**
 * 获取一个文件夹下所有的文件并转换为Config类型并操作
 *
 * @param file 文件夹
 * @param def 默认类型
 * @param action 配置文件操作
 */
fun readFolderWalkConfig(file: File, action: FolderReader.() -> Unit) {
    FolderReader(file).also(action)
}

/**
 * 灵活释放当前插件内特定目录下的所有资源文件并读取
 * 1. 如果不存在则释放资源文件
 * 2. 如果存在则直接读取文件内容
 *
 * @param path 资源路径
 * @param action 文件操作
 */
fun releaseResourceFolderAndRead(path: String, action: FolderReader.() -> Unit) {
    val file = File(getDataFolder(), path)
    if (!file.exists()) {
        releaseResourceFolder(path)
    }
    FolderReader(file).also(action)
}

data class FolderReader(val file: File) {
    private val readTypes = mutableListOf(Type.YAML)
    private val filter = mutableListOf<File.() -> Boolean>()

    fun setReadType(vararg type: Type) {
        readTypes.clear()
        readTypes.addAll(type)
    }

    fun addFilter(filter: File.() -> Boolean) {
        this.filter.add(filter)
    }

    fun clearFilter() {
        filter.clear()
    }

    fun walk(action: Configuration.() -> Unit) {
        val walk = file.walk().filter { it.isFile }.filter { getTypeFromExtension(it.extension) in readTypes }
        filter.forEach { walk.filter(it) }
        walk.forEach { inline ->
            loadFromFile(inline, getTypeFromExtension(inline.extension)).also(action)
        }
    }
}

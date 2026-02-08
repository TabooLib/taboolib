@file:Suppress("DEPRECATION")

package taboolib.module.lang

import taboolib.common.io.newFile
import taboolib.common.io.runningResourcesInJar
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.function.warning
import taboolib.common.util.replaceWithOrder
import taboolib.common.util.t
import taboolib.common5.FileWatcher
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.SecuredFile
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

/**
 * TabooLib
 * taboolib.module.lang.ResourceReader
 *
 * @author sky
 * @since 2021/6/21 11:48 下午
 */
class ResourceReader(val clazz: Class<*>, val migrate: Boolean = true) {

    val files = HashMap<String, LanguageFile>()
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")

    init {
        Language.languageCode.forEach { code ->
            val fileName = runningResourcesInJar.keys.first { it.startsWith("${Language.path}/$code") }
            val bytes = runningResourcesInJar[fileName]
            if (bytes != null) {
                val nodes = HashMap<String, Type>()
                val source = bytes.toString(StandardCharsets.UTF_8)
                val fileType = Configuration.getTypeFromExtension(fileName.substringAfterLast('.'))
                val sourceFile = Configuration.loadFromString(source, fileType)
                // 加载内存中的原件
                loadNodes(sourceFile, nodes, code)
                // 释放文件
                val file = newFile(Language.releasePath.replaceWithOrder(pluginId, fileName.substringAfterLast('/')))
                if (file.length() == 0L) {
                    file.writeBytes(bytes)
                }
                // 移除文件监听
                if (Language.enableFileWatcher) {
                    FileWatcher.INSTANCE.removeListener(file)
                }
                val exists = HashMap<String, Type>()
                // 加载文件
                loadNodes(Configuration.loadFromFile(file), exists, code)
                // 检查缺失
                val missingKeys = nodes.keys.filter { !exists.containsKey(it) }
                if (missingKeys.isNotEmpty() && migrate) {
                    // 更新文件
                    migrateFile(missingKeys, sourceFile, file)
                }
                nodes += exists
                files[code] = LanguageFile(file, nodes).also {
                    files[code] = it
                    // 文件变动监听
                    if (Language.enableFileWatcher) {
                        FileWatcher.INSTANCE.addSimpleListener(file) { _ ->
                            it.nodes.clear()
                            loadNodes(sourceFile, it.nodes, code)
                            loadNodes(Configuration.loadFromFile(file), it.nodes, code)
                        }
                    }
                }
            } else {
                val file = "$code.${fileName.substringAfterLast('.')}"
                warning(
                    """
                        未能找到语言文件: $file
                        Missing language file: $file
                    """.t()
                )
            }
        }
    }

    /**
     * 从配置文件中加载语言节点
     *
     * @param file 配置文件对象
     * @param nodesMap 用于存储加载的节点的映射
     * @param code 语言代码
     */
    fun loadNodes(file: Configuration, nodesMap: HashMap<String, Type>, code: String) {
        // 加载节点
        file.getKeys(false).forEach { node ->
            when (val obj = file[node]) {
                // 列表
                is List<*> -> {
                    nodesMap[node] = TypeList(obj.mapNotNull { sub ->
                        if (sub is Map<*, *>) {
                            loadNode(sub.map { it.key.toString() to it.value!! }.toMap(), code, node)
                        } else {
                            TypeText(sub.toString())
                        }
                    })
                }
                // 嵌套
                is ConfigurationSection -> {
                    val type = loadNode(obj.getValues(false).map { it.key to it.value!! }.toMap(), code, node)
                    if (type != null) {
                        nodesMap[node] = type
                    }
                }
                // 其他
                else -> nodesMap[node] = TypeText(obj.toString())
            }
        }
    }

    /**
     * 从映射中加载语言节点
     *
     * @param map 包含节点信息的映射
     * @param code 语言代码
     * @param node 节点名称（可为空）
     * @return 加载的语言类型实例，如果加载失败则返回 null
     */
    private fun loadNode(map: Map<String, Any>, code: String, node: String?): Type? {
        return if (map.containsKey("type") || map.containsKey("==")) {
            val type = (map["type"] ?: map["=="]).toString().lowercase()
            val typeInstance = Language.languageType[type]?.getDeclaredConstructor()?.newInstance()
            if (typeInstance != null) {
                typeInstance.init(map)
            } else {
                warning(
                    """
                        不支持的语言文件类型: $node > $type ($code)
                        Unsupported language type: $node > $type ($code)
                    """.t()
                )
            }
            typeInstance
        } else {
            warning(
                """
                    语言文件类型缺失: $node ($code)
                    Missing language type: $map ($code)
                """.t()
            )
            null
        }
    }

    /**
     * 迁移文件，将缺失的键值对添加到目标文件中
     *
     * @param missing 缺失的键列表
     * @param source 源配置对象，包含所有键值对
     * @param file 目标文件，用于追加缺失的键值对
     */
    @Suppress("DEPRECATION")
    private fun migrateFile(missing: List<String>, source: Configuration, file: File) {
        submitAsync {
            val append = ArrayList<String>()
            append += "# ------------------------- #"
            append += "#  UPDATE ${dateFormat.format(System.currentTimeMillis())}  #"
            append += "# ------------------------- #"
            append += ""
            missing.forEach { key ->
                val obj = source[key]
                if (obj != null) {
                    append += SecuredFile.dumpAll(key, obj)
                }
            }
            file.appendText("\n${append.joinToString("\n")}")
        }
    }
}
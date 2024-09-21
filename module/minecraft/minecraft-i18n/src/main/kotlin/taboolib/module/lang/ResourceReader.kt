@file:Suppress("DEPRECATION")

package taboolib.module.lang

import taboolib.common.io.newFile
import taboolib.common.io.runningResourcesInJar
import taboolib.common.platform.function.debug
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.function.warning
import taboolib.common.util.replaceWithOrder
import taboolib.common5.FileWatcher
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.SecuredFile
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.regex.Pattern

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
                if (isFileWatcherHook) {
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
                    if (isFileWatcherHook) {
                        FileWatcher.INSTANCE.addSimpleListener(file) {
                            it.nodes.clear()
                            loadNodes(sourceFile, it.nodes, code)
                            loadNodes(Configuration.loadFromFile(file), it.nodes, code)
                        }
                    }
                }
            } else {
                warning("Missing language file: $code.${fileName.substringAfterLast('.')}")
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
        // 迁移旧版本
        migrateLegacyVersion(file)
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
     * 获取所有有效的语言文件节点
     */
    private fun ConfigurationSection.getLanguageNodes(): Map<String, Any?> {
        return getValues(true).filter { it.key.endsWith(".==") || it.key.endsWith(".type") }
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
                warning("Unsupported language type: $node > $type ($code)")
            }
            typeInstance
        } else {
            warning("Missing language type: $map ($code)")
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

    /**
     * 迁移旧版本的配置文件格式
     *
     * 此函数用于处理旧版本的配置文件，将其转换为新的格式。主要进行以下操作：
     * 1. 将包含点号的键名替换为使用连字符的键名
     * 2. 处理特殊的值类型，如 ConfigurationSection 和 List
     * 3. 对空字符串进行特殊处理
     *
     * @param file 需要迁移的配置文件对象
     */
    private fun migrateLegacyVersion(file: Configuration) {
        if (file.file == null) {
            return
        }
        var fixed = false
        val values = file.getLanguageNodes().toSortedMap()
        values.forEach {
            if (it.key.contains('.')) {
                fixed = true
                file[it.key.substringBefore('.')] = null
                file[it.key.replace('.', '-')] = when (val obj = it.value) {
                    // 处理 ConfigurationSection
                    is ConfigurationSection -> migrateLegacyJsonType(obj)
                    // 处理列表
                    is List<*> -> {
                        obj.map { element ->
                            when (element) {
                                is Map<*, *> -> migrateLegacyJsonType(element.mapKeys { entry -> entry.key.toString() }.toSection(Configuration.empty()))
                                is String -> element.ifEmpty { "&r" }
                                else -> element
                            }
                        }
                    }
                    // 跳过
                    else -> obj
                }
                debug("Migrate language: ${it.key}: ${it.key.substringBefore('.')} -> ${it.key.replace('.', '-')}: ${file[it.key.replace('.', '-')]}")
            }
        }
        if (fixed) {
            file.saveToFile()
        }
    }

    /**
     * 对老版本的 Json 写法进行迁移更新
     *
     * @param section 需要迁移的 ConfigurationSection 对象
     * @return 迁移后的 ConfigurationSection 对象
     */
    private fun migrateLegacyJsonType(section: ConfigurationSection): ConfigurationSection {
        val type = section.getString("==", section.getString("type")) ?: return section
        if (type.lowercase() != "json") {
            return section
        }
        var text = section.getString("text") ?: return section
        val argSection = section.getConfigurationSection("args") ?: return section
        // 对已有的变量进行转义
        text = text.replace("[", "\\[").replace("]", "\\]")
        val args = argSection.getKeys(false).mapNotNull { argSection.getConfigurationSection(it) }.associate { it.name to it.getValues(false) }
        val newArgs = ArrayList<Map<String, Any?>>()
        val matcher = legacyArgsRegex.matcher(text)
        while (matcher.find()) {
            val full = matcher.group(0)
            val display = matcher.group(1)
            val node = matcher.group(2)
            val body = args[node]
            if (body == null) {
                text = text.replace(full, display)
                continue
            }
            text = text.replace(full, "[$display]")
            newArgs += body
        }
        section["text"] = text
        section["args"] = newArgs
        return section
    }

    private fun Map<*, *>.toSection(root: ConfigurationSection): ConfigurationSection {
        forEach { (key, value) ->
            when (value) {
                is Map<*, *> -> root[key.toString()] = value.toSection(root.createSection(key.toString()))
                else -> root[key.toString()] = value
            }
        }
        return root
    }

    companion object {

        private val legacyArgsRegex = Pattern.compile("<(.+)@(.+)>")

        private val isFileWatcherHook by lazy {
            try {
                FileWatcher.INSTANCE
                true
            } catch (ex: NoClassDefFoundError) {
                false
            }
        }
    }
}
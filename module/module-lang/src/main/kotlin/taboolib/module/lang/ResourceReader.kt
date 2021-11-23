package taboolib.module.lang

import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.warning
import taboolib.common5.FileWatcher
import taboolib.library.configuration.ConfigurationSection
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
            val resourceAsStream = clazz.classLoader.getResourceAsStream("lang/$code.yml")
            if (resourceAsStream != null) {
                val nodes = HashMap<String, Type>()
                val source = resourceAsStream.readBytes().toString(StandardCharsets.UTF_8)
                val sourceFile = SecuredFile.loadConfiguration(source)
                // 加载内存中的原件
                loadNodes(sourceFile, nodes, code)
                // 释放文件
                val file = releaseResourceFile("lang/$code.yml")
                // 移除文件监听
                if (isFileWatcherHook) {
                    FileWatcher.INSTANCE.removeListener(file)
                }
                val exists = HashMap<String, Type>()
                // 加载文件
                loadNodes(SecuredFile.loadConfiguration(file), exists, code)
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
                            loadNodes(SecuredFile.loadConfiguration(file), it.nodes, code)
                        }
                    }
                }
            }
        }
    }

    @Suppress("SimplifiableCallChain")
    fun loadNodes(file: SecuredFile, nodesMap: HashMap<String, Type>, code: String) {
        migrateLegacyVersion(file)
        file.getKeys(false).forEach { node ->
            when (val obj = file.get(node)) {
                is String -> {
                    nodesMap[node] = TypeText(obj)
                }
                is List<*> -> {
                    nodesMap[node] = TypeList(obj.mapNotNull { sub ->
                        if (sub is Map<*, *>) {
                            loadNode(sub.map { it.key.toString() to it.value!! }.toMap(), code, node)
                        } else {
                            TypeText(sub.toString())
                        }
                    })
                }
                is ConfigurationSection -> {
                    val type = loadNode(obj.getValues(false).map { it.key to it.value!! }.toMap(), code, node)
                    if (type != null) {
                        nodesMap[node] = type
                    }
                }
                else -> {
                    warning("Unsupported language node: $node ($code)")
                }
            }
        }
    }

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

    private fun migrateFile(missing: List<String>, source: SecuredFile, file: File) {
        submit(async = true) {
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

    private fun migrateLegacyVersion(file: SecuredFile) {
        if (file.file == null) {
            return
        }
        var fixed = false
        val values = file.getValues(HashMap(), "").toSortedMap()
        values.forEach {
            if (it.key.contains('.')) {
                fixed = true
                file.set(it.key.substringBefore('.'), null)
                file.set(it.key.replace('.', '-'), when (val obj = it.value) {
                    is ConfigurationSection -> migrateLegacyJsonType(obj)
                    is List<*> -> {
                        obj.map { element ->
                            when (element) {
                                is Map<*, *> -> migrateLegacyJsonType(element.mapKeys { entry -> entry.key.toString() }.toSection(SecuredFile()))
                                is String -> element.ifEmpty { "&r" }
                                else -> element
                            }
                        }
                    }
                    else -> obj
                })
            }
        }
        if (fixed) {
            file.saveToFile()
        }
    }

    /**
     * 对老版本的 Json 写法进行迁移更新
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

    /**
     * 获取所有键值对，同 getValues 方法。
     * 在经过 == 或是 type 时停止
     */
    private fun ConfigurationSection.getValues(collect: HashMap<String, Any?>, node: String): HashMap<String, Any?> {
        var key = node
        if (node.isNotEmpty()) {
            key += "."
        }
        key += name
        if (contains("==") || contains("type")) {
            collect[key] = this
        } else {
            getKeys(false).forEach {
                when (val obj = get(it)) {
                    is ConfigurationSection -> obj.getValues(collect, key)
                    else -> {
                        var nextKey = key
                        if (key.isNotEmpty()) {
                            nextKey += "."
                        }
                        nextKey += it
                        collect[nextKey] = obj
                    }
                }
            }
        }
        return collect
    }

    private fun Map<*, *>.toSection(root: ConfigurationSection): ConfigurationSection {
        forEach { (key, value) ->
            when (value) {
                is Map<*, *> -> {
                    root[key.toString()] = value.toSection(root.createSection(key.toString()))
                }
                else -> {
                    root[key.toString()] = value
                }
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
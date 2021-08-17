package taboolib.module.lang

import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.warning
import taboolib.common5.FileWatcher
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.configuration.YamlConfiguration
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
                    val type =
                        loadNode(obj.getValues(false).map { it.key.toString() to it.value!! }.toMap(), code, node)
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

    private fun migrateOldJson(section: ConfigurationSection): ConfigurationSection {
        val type = section.getString("==", section.getString("type")) ?: return section
        if (type.lowercase() != "json") {
            return section
        }

        var text = section.getString("text") ?: return section
        val argSection = section.getConfigurationSection("args") ?: return section

        text = text.replace("[", "\\[").replace("]", "\\]")
        val args = argSection.getKeys(false)
            .mapNotNull { argSection.getConfigurationSection(it) }
            .associate { it.name to it.getValues(false) }
        val newArgs = mutableListOf<Map<String, Any>>()

        // 反正你也要改代码的我就写注释给你看
        // 这里现场 compile Pattern 你可能看了高血压
        // 但是我也知道不应该这样, 但是我不知道放哪里
        val pattern = Pattern.compile("<(.+)@(.+)>")
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val full = matcher.group(0)
            val display = matcher.group(1)
            val arg = matcher.group(2)

            val argContext = args[arg]
            if (argContext == null) {
                text = text.replace(full, display)
                continue
            }

            text = text.replace(full, "[$display]")
            newArgs.add(argContext)
        }

        section.set("text", text)
        section.set("args", newArgs)
        return section
    }

    private fun chainValue(
        lastKey: String,
        section: ConfigurationSection,
        collect: HashMap<String, Any>
    ): HashMap<String, Any> {
        var key = lastKey
        if (lastKey.isNotEmpty()) {
            key += "."
        }
        key += section.name

        if (section.contains("==") || section.contains("type")) {
            collect[key] = section
        } else {
            section.getKeys(false).forEach {
                when (val obj = section.get(it)) {
                    is ConfigurationSection -> {
                        chainValue(key, obj, collect)
                    }
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

    private fun createConfigurationSection(map: Map<*, *>, root: ConfigurationSection): ConfigurationSection {
        map.forEach { (key, value) ->
            when (value) {
                is Map<*, *> -> {
                    root[key.toString()] = createConfigurationSection(value, root.createSection(key.toString()))
                }
                else -> root[key.toString()] = value
            }
        }
        return root
    }

    private fun migrateLegacyVersion(file: SecuredFile) {
        if (file.file == null) {
            return
        }
        var fixed = false

        val values = chainValue("", file, hashMapOf()).toSortedMap()
        values.forEach {
            if (it.key.contains('.')) {
                fixed = true
                file.set(it.key.substringBefore('.'), null)
                file.set(it.key.replace('.', '-'), when (val obj = it.value) {
                    is ConfigurationSection -> migrateOldJson(obj)
                    is List<*> -> {
                        obj.map { element ->
                            when (element) {
                                is Map<*, *> -> {
                                    migrateOldJson(
                                        createConfigurationSection(
                                            element.mapKeys { entry -> entry.key.toString() },
                                            YamlConfiguration()
                                        )
                                    )
                                }
                                is String -> {
                                    element.ifEmpty {
                                        "&r"
                                    }
                                }
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

    companion object {

        val isFileWatcherHook by lazy {
            try {
                FileWatcher.INSTANCE
                true
            } catch (ex: NoClassDefFoundError) {
                false
            }
        }
    }
}
package taboolib.module.lang

import taboolib.common.platform.releaseResourceFile
import taboolib.common.platform.submit
import taboolib.common.platform.warning
import taboolib.common5.FileWatcher
import taboolib.library.configuration.ConfigurationSection
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
        checkLegacyVersion(file)
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
                    val type = loadNode(obj.getValues(false).map { it.key.toString() to it.value!! }.toMap(), code, node)
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
        return if (map.containsKey("type")) {
            val type = map["type"].toString().lowercase()
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

    private fun checkLegacyVersion(file: SecuredFile) {
        var fixed = false
        file.getValues(true).forEach {
            if (it.key.contains('.')) {
                fixed = true
                file.set(it.key, null)
                file.set(it.key.replace('.', '-'), it.value)
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
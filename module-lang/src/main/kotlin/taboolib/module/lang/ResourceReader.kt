package taboolib.module.lang

import taboolib.common.platform.submit
import taboolib.common.platform.getDataFolder
import taboolib.common.platform.warning
import taboolib.common5.io.FileWatcher
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
        val folder = File(getDataFolder(), "lang")
        Language.languageCode.forEach { code ->
            val resourceAsStream = clazz.getResourceAsStream("lang/$code.yml")
            if (resourceAsStream != null) {
                val nodes = HashMap<String, Type>()
                val source = resourceAsStream.readBytes().toString(StandardCharsets.UTF_8)
                val sourceFile = SecuredFile.loadConfiguration(source)
                // 加载内存中的原件
                loadNodes(sourceFile, nodes, code)
                val file = File(folder, "$code.yml")
                if (isFileWatcherHook) {
                    FileWatcher.INSTANCE.removeListener(file)
                }
                if (!file.exists()) {
                    file.createNewFile()
                    file.writeText(source, StandardCharsets.UTF_8)
                }
                val exists = HashMap<String, Type>()
                // 加载文件
                loadNodes(SecuredFile.loadConfiguration(file), exists, code)
                // 检查缺失
                val missingKeys = nodes.keys.filter { !exists.containsKey(it) }
                if (missingKeys.isNotEmpty() && migrate) {
                    // 更新
                    submit(async = true) {
                        val append = ArrayList<String>()
                        append += "# ------------------------- #"
                        append += "#  UPDATE ${dateFormat.format(System.currentTimeMillis())}  #"
                        append += "# ------------------------- #"
                        append += ""
                        missingKeys.forEach { key ->
                            val obj = sourceFile[key]
                            if (obj != null) {
                                append += SecuredFile.dumpAll(key, obj)
                            }
                        }
                        file.appendText("\n${append.joinToString("\n")}")
                    }
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

    fun loadNodes(sourceFile: SecuredFile, nodesMap: HashMap<String, Type>, languageCode: String) {
        sourceFile.getKeys(false).forEach { node ->
            when (val obj = sourceFile.get(node)) {
                is String -> {
                    nodesMap[node] = TypeText().also { it.init(mapOf("text" to obj)) }
                }
                is List<*> -> {
                    obj.forEach { sub ->
                        if (sub is Map<*, *>) {
                            val map = sub.map { it.key.toString() to it.value!! }.toMap()
                            if (map.containsKey("type")) {
                                val type = map["type"].toString()
                                val typeInstance = Language.languageType[type]?.getDeclaredConstructor()?.newInstance()
                                if (typeInstance != null) {
                                    typeInstance.init(map)
                                    nodesMap[node] = typeInstance
                                } else {
                                    warning("unsupported language type: $node > $type ($languageCode)")
                                }
                            }
                        } else {
                            warning("unsupported language node: $node ($languageCode)")
                        }
                    }
                }
                else -> {
                    warning("unsupported language node: $node ($languageCode)")
                }
            }
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
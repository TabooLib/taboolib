package taboolib.common.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.io.newFile
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Supplier

/**
 * TabooLib
 * taboolib.common.reflect.ClassDefine
 *
 * @author 坏黑
 * @since 2024/7/6 09:35
 */
class ClassMarkers(val version: String) {

    val markers = ConcurrentHashMap<String, MutableSet<String>>()

    /** 文件保存路径 */
    val path = try {
        "taboolib".substring(0, "taboolib".length - 9);
    } catch (_: Throwable) {
        "taboolib"
    }

    /** 缓存文件 */
    val file = File("cache/taboolib/$path/markers.json")

    /** 是否变更 */
    var isChanged = false

    init {
        // 读取缓存文件
        read()
        // 关闭服务器时保存缓存文件
        TabooLib.registerLifeCycleTask(LifeCycle.DISABLE, 999, ::save)
    }

    fun match(group: String, className: String, process: Supplier<Boolean>): Boolean {
        val classes = markers.getOrPut(group) { CopyOnWriteArraySet() }
        // 如果变更则不检查
        if (isChanged) {
            val check = process.get()
            if (check) classes += className
            return check
        }
        return classes.contains(className)
    }

    private fun save() {
        val root = JsonObject()
        root.addProperty("version", version)
        val groups = JsonObject()
        markers.forEach { (k, v) ->
            val array = JsonArray()
            v.forEach { array.add(it) }
            groups.add(k, array)
        }
        root.add("groups", groups)
        // 写入文件
        newFile(file).writeText(root.toString())
    }

    private fun read() {
        if (!file.exists()) {
            isChanged = true
            return
        }
        // 读取文件内容并转换为 JsonObject
        val root = JsonParser().parse(file.bufferedReader()).asJsonObject
        val groups = root.getAsJsonObject("groups")
        // 版本判定
        if (version != root.getAsJsonPrimitive("version")?.asString || groups == null) {
            isChanged = true
            return
        }
        groups.entrySet().forEach { (k, v) ->
            markers[k] = v.asJsonArray.map { it.asString }.toMutableSet()
        }
    }
}
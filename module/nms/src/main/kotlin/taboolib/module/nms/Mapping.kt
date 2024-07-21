package taboolib.module.nms

import com.google.gson.JsonParser
import taboolib.common.PrimitiveIO
import taboolib.common.env.RuntimeEnv
import taboolib.common.io.runningResources
import taboolib.common.platform.function.warning
import taboolib.common.util.unsafeLazy
import java.io.InputStream
import java.util.*

/**
 * TabooLib
 * taboolib.module.nms.Mapping
 *
 * @author sky
 * @since 2021/6/17 10:59 下午
 */
class Mapping {

    // <Spigot.SimpleName, Spigot.FullName>
    val classMapSpigotS2F = HashMap<String, String>()

    // 内存换性能
    // <Spigot.FullName, Mojang.FullName>
    val classMapSpigotToMojang = HashMap<String, String>()
    // <Mojang.FullName, Spigot.FullName>
    val classMapMojangToSpigot = HashMap<String, String>()

    val fields = LinkedList<Field>()
    val methods = LinkedList<Method>() // 1.18 only

    companion object {

        /**
         * 读取 Spigot 格式的映射文件
         */
        fun spigot(inputStreamCombined: InputStream, inputStreamFields: InputStream): Mapping {
            val time = System.currentTimeMillis()
            val mapping = Mapping()
            // 解析类名映射
            inputStreamCombined.use {
                it.bufferedReader().forEachLine { line ->
                    if (line.startsWith('#')) {
                        return@forEachLine
                    }
                    if (line.contains(' ')) {
                        val name = line.substringAfterLast(' ')
                        mapping.classMapSpigotS2F[name.substringAfterLast('/', "")] = name
                    }
                }
            }
            // 解析字段映射
            inputStreamFields.use {
                it.bufferedReader().forEachLine { line ->
                    if (line.startsWith('#')) {
                        return@forEachLine
                    }
                    val args = line.split(' ')
                    if (args.size >= 3) {
                        // 1.18 开始支持方法映射
                        if (args[2].startsWith('(')) {
                            val name = args.last()
                            val parameter = args[args.size - 2]
                            mapping.methods += Method(args[0].replace('/', '.'), args[1], name, parameter)
                        } else {
                            mapping.fields += Field(args[0].replace('/', '.'), args[1], args[2])
                        }
                    }
                }
            }
            PrimitiveIO.dev("Spigot Mapping Loaded. (${System.currentTimeMillis() - time}ms)")
            PrimitiveIO.dev("Classes: ${mapping.classMapSpigotS2F.size}, Fields: ${mapping.fields.size}, Methods: ${mapping.methods.size}")
            return mapping
        }

        /**
         * 读取 Paper 格式 (reobf.tiny) 的映射文件
         */
        fun paper(): Mapping {
            val time = System.currentTimeMillis()
            val mapping = Mapping()
            val inputStream = obcClass("CraftServer").classLoader.getResourceAsStream("META-INF/mappings/reobf.tiny") ?: return mapping
            inputStream.use {
                var i = 0
                var mojangName = ""
                it.bufferedReader().forEachLine { line ->
                    // 第一行忽略
                    if (i++ == 0) return@forEachLine
                    // 成员
                    val args = line.split('	')
                    // 类
                    // Paper 在运行时会将类转换为 Mojang Deobf 名
                    if (args[0] == "c") {
                        mojangName = args[1].replace('/', '.')
                        val spigotName = args[2].replace('/', '.')
                        mapping.classMapSpigotToMojang[spigotName] = mojangName
                        mapping.classMapMojangToSpigot[mojangName] = spigotName
                    }
                    // 方法
                    // Paper 在运行时会将方法转换为 Mojang Deobf 名，但 Spigot 不会（Spigot 环境时，方法名为 Mojang Obf 名）
                    else if (args[1] == "m") {
                        mapping.methods += Method(
                            mojangName,
                            args[4], // Mojang Deobf
                            args[3], // Mojang Obf
                            args[2]  // descriptor
                        )
                    }
                    // 字段
                    // Paper 在运行时会将字段转换为 Mojang Deobf 名，但 Spigot 不会（Spigot 环境时，字段名为 Mojang Obf 名）
                    else if (args[1] == "f") {
                        mapping.fields += Field(
                            mojangName,
                            args[4], // Mojang Deobf
                            args[3]  // Mojang Obf
                        )
                    }
                }
            }
            PrimitiveIO.dev("Paper Mapping Loaded. (${System.currentTimeMillis() - time}ms)")
            PrimitiveIO.dev("Classes: ${mapping.classMapSpigotToMojang.size}, Fields: ${mapping.fields.size}, Methods: ${mapping.methods.size}")
            return mapping
        }
    }

    /**
     * 字段映射
     */
    data class Field(val path: String, val mojangName: String, val translateName: String) {

        val className = path.substringAfterLast('.', "")
    }

    /**
     * 方法映射，1.18+
     */
    data class Method(val path: String, val mojangName: String, val translateName: String, val descriptor: String) {

        val className = path.substringAfterLast('.', "")
    }
}

class SpigotMapping(val combined: String, val fields: String) {

    companion object {

        const val OSS_URL = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/"

        /**
         * 当前运行环境所对应的 Spigot Mapping 文件
         */
        val current: SpigotMapping? by unsafeLazy {
            val mappingJson = runningResources["mapping.json"]
            if (mappingJson == null) {
                warning("Resource \"mapping.json\" not found.")
                return@unsafeLazy null
            }
            // 获取当前运行版本
            val version = if (MinecraftVersion.isUniversal) MinecraftVersion.runningVersion else "1.17"
            // 解析文件
            JsonParser().parse(mappingJson.decodeToString()).asJsonArray.forEach {
                val obj = it.asJsonObject
                if (version == obj["version"].asString) {
                    // 解析 Json
                    val combined = obj["combined"].asJsonObject
                    val combinedHash = combined["hash"].asString
                    val fields = obj["fields"].asJsonObject
                    val fieldsHash = fields["hash"].asString
                    // 下载资源文件
                    RuntimeEnv.ENV.loadAssets("", combinedHash, "$OSS_URL${combined["file"].asString}", true)
                    RuntimeEnv.ENV.loadAssets("", fieldsHash, "$OSS_URL${fields["file"].asString}", true)
                    return@unsafeLazy SpigotMapping(combinedHash, fieldsHash)
                }
            }
            null
        }
    }
}
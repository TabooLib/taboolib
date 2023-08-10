package taboolib.module.nms

import taboolib.common.util.join
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * TabooLib
 * taboolib.module.nms.Mapping
 *
 * @author sky
 * @since 2021/6/17 10:59 下午
 */
class Mapping(inputStreamCombined: InputStream, inputStreamFields: InputStream) {

    val classMap = LinkedHashMap<String, String>()
    val fields = LinkedList<Field>()
    val methods = LinkedList<Method>() // 1.18 only

    init {
        // 解析类名映射
        inputStreamCombined.use {
            it.readBytes().toString(StandardCharsets.UTF_8).lines().forEach { line ->
                if (line.startsWith('#')) {
                    return@forEach
                }
                val args = line.split(' ')
                if (args.size == 2) {
                    classMap[args[1].substringAfterLast('/', "")] = args[1]
                }
            }
        }
        // 解析字段映射
        inputStreamFields.use {
            it.readBytes().toString(StandardCharsets.UTF_8).lines().forEach { line ->
                if (line.startsWith('#')) {
                    return@forEach
                }
                val args = line.split(' ')
                if (args.size >= 3) {
                    // 1.18 开始支持方法映射
                    if (args[2].startsWith("(")) {
                        val info = join(args.toTypedArray(), 2)
                        val name = info.substringAfterLast(' ')
                        val parameter = info.substringBeforeLast(' ')
                        methods += Method(args[0].replace("/", "."), args[1], name, parameter)
                    } else {
                        fields += Field(args[0].replace("/", "."), args[1], args[2])
                    }
                }
            }
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
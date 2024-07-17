package taboolib.module.nms

import taboolib.common.PrimitiveIO
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
            it.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.startsWith('#')) {
                        return@forEach
                    }
                    if (line.contains(' ')) {
                        val name = line.substringAfterLast(' ')
                        classMap[name.substringAfterLast('/', "")] = name
                    }
                }
            }
        }
        // 解析字段映射
        inputStreamFields.use {
            it.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.startsWith('#')) {
                        return@forEach
                    }
                    val args = line.split(' ')
                    if (args.size >= 3) {
                        // 1.18 开始支持方法映射
                        if (args[2].startsWith('(')) {
                            val name = args.last()
                            val parameter = args[args.size - 2]
                            methods += Method(args[0].replace('/', '.'), args[1], name, parameter)
                        } else {
                            fields += Field(args[0].replace('/', '.'), args[1], args[2])
                        }
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
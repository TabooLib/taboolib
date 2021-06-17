package taboolib.module.nms

import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * TabooLib
 * taboolib.module.nms.Mapping
 *
 * @author sky
 * @since 2021/6/17 10:59 下午
 */
class Mapping(inputStream: InputStream) {

    val fields = ArrayList<Field>()
    val classMap = HashMap<String, String>()

    init {
        inputStream.readBytes().toString(StandardCharsets.UTF_8).lines().forEach { line ->
            if (line.startsWith('#')) {
                return@forEach
            }
            val args = line.split(' ')
            if (args.size == 3) {
                fields += Field(args[0].replace("/", "."), args[1], args[2])
            }
        }
        fields.forEach {
            classMap[it.path.substringAfterLast('.', "")] = it.path
        }
    }

    class Field(val path: String, val mojangName: String, val translateName: String) {

        val className = path.substringAfterLast('.', "")
    }
}
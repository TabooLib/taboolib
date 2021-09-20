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
class Mapping(inputStreamCombined: InputStream, inputStreamFields: InputStream) {

    val classMap = HashMap<String, String>()
    val fields = ArrayList<Field>()

    init {
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
        inputStreamFields.use {
            it.readBytes().toString(StandardCharsets.UTF_8).lines().forEach { line ->
                if (line.startsWith('#')) {
                    return@forEach
                }
                val args = line.split(' ')
                if (args.size == 3) {
                    fields += Field(args[0].replace("/", "."), args[1], args[2])
                }
            }
        }
    }

    class Field(val path: String, val mojangName: String, val translateName: String) {

        val className = path.substringAfterLast('.', "")
    }
}
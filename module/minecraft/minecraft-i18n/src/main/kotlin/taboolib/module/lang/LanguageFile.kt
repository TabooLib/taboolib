package taboolib.module.lang

import java.io.File

/**
 * TabooLib
 * taboolib.module.lang.LanguageFile
 *
 * @author sky
 * @since 2021/6/18 11:04 下午
 */
class LanguageFile(val file: File, nodes: HashMap<String, Type>) {

    val nodes: HashMap<String, Type> = SnapshotHashMap(nodes)

    @JvmSynthetic
    internal fun replaceNodes(nodes: HashMap<String, Type>) {
        (this.nodes as SnapshotHashMap<String, Type>).replaceWith(nodes)
    }
}

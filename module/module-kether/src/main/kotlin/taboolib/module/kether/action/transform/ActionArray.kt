package taboolib.module.kether.action.transform

import taboolib.common.Inject
import taboolib.module.kether.*

/**
 * Zaphkiel
 * ink.ptms.zaphkiel.module.kether.ActionElement
 *
 * @author sky
 * @since 2021/3/16 2:56 下午
 */
@Inject
internal object ActionArray {

    /**
     * size &array
     */
    @KetherParser(["size", "length"])
    fun actionSize() = combinationParser {
        it.group(any()).apply(it) { arr ->
            now {
                when (arr) {
                    is Collection<*> -> arr.size
                    is Array<*> -> arr.size
                    else -> arr.toString().length
                }
            }
        }
    }

    /**
     * 转换为可变列表
     */
    @KetherParser(["mutable"])
    fun actionMutable() = combinationParser {
        it.group(anyAsList()).apply(it) { array -> now { array.toMutableList() } }
    }

    /**
     * 打乱列表
     */
    @KetherParser(["shuffle"])
    fun actionShuffle() = combinationParser {
        it.group(anyAsList()).apply(it) { array -> now { array.shuffled().toMutableList() } }
    }

    /**
     * 反转列表
     */
    @KetherParser(["reverse"])
    fun actionReverse() = combinationParser {
        it.group(anyAsList()).apply(it) { array -> now { array.reversed().toMutableList() } }
    }

    /**
     * 构建列表
     */
    @KetherParser(["array", "arr"])
    fun actionArray() = combinationParser {
        it.group(originList()).apply(it) { array -> now { array } }
    }

    /**
     * 获取列表中的元素
     * arr-get 1 in &array
     */
    @KetherParser(["arr-get", "element", "elem"])
    fun actionArrayGet() = combinationParser {
        it.group(int(), command("in", "of", then = anyAsList())).apply(it) { el, array -> now { array.getOrNull(el) } }
    }

    /**
     * 添加元素到列表末尾
     * arr-add test to &array
     */
    @KetherParser(["arr-add"])
    fun actionArrayAdd() = combinationParser {
        it.group(any(), command("to", then = anyAsList())).apply(it) { el, array -> now { array.add(el) } }
    }

    /**
     * 添加元素到列表末尾
     * arr-add test to &array
     */
    @KetherParser(["arr-add-first", "arr-push"])
    fun actionArrayAddFirst() = combinationParser {
        it.group(any(), command("to", then = anyAsList())).apply(it) { el, array -> now { array.add(0, el) } }
    }

    /**
     * 移除列表中的元素
     * arr-remove test in &array
     */
    @KetherParser(["arr-remove"])
    fun actionArrayRemove() = combinationParser {
        it.group(any(), command("in", then = anyAsList())).apply(it) { el, array -> now { array.remove(el) } }
    }

    /**
     * 移除列表中的元素
     * arr-remove-at 1 in &array
     */
    @KetherParser(["arr-remove-at"])
    fun actionArrayRemoveAt() = combinationParser {
        it.group(int(), command("in", then = anyAsList())).apply(it) { el, array -> now { array.removeAt(el) } }
    }

    /**
     * 移除列表中的首个元素
     * arr-remove-first &array
     */
    @KetherParser(["arr-remove-first", "arr-take"])
    fun actionArrayRemoveFirst() = combinationParser {
        it.group(anyAsList()).apply(it) { array -> now { array.removeFirstOrNull() } }
    }

    /**
     * 移除列表中的末尾元素
     * arr-remove-last &array
     */
    @KetherParser(["arr-remove-last", "arr-drop"])
    fun actionArrayRemoveLast() = combinationParser {
        it.group(anyAsList()).apply(it) { array -> now { array.removeLastOrNull() } }
    }

    /**
     * 移除列表中的元素
     * arr-find test in &array
     */
    @KetherParser(["arr-find"])
    fun actionArrayFind() = combinationParser {
        it.group(any(), command("in", "of", then = anyAsList())).apply(it) { el, array -> now { array.indexOf(el) } }
    }
}
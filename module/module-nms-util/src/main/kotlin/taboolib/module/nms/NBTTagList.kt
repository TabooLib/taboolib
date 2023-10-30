package taboolib.module.nms

import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer

/**
 * TabooLib
 * taboolib.module.nms.NBTTagList
 *
 * @author 坏黑
 * @since 2023/8/10 01:17
 */
class NBTTagList : NBTTagData, MutableList<NBTTagData> {

    private val value = CopyOnWriteArrayList<NBTTagData>()

    constructor() : super(NBTTagType.LIST, 0) {
        this.data = this
    }

    constructor(list: List<NBTTagData>) : super(NBTTagType.LIST, 0) {
        this.data = this
        this.value += list
    }

    override fun asList(): NBTTagList {
        return this
    }

    override fun toJsonSimplified(): String {
        return toJsonSimplified(0)
    }

    override fun toJsonSimplified(index: Int): String {
        val builder = StringBuilder()
        builder.append("[\n")
        value.forEach(Consumer {
            builder.append("  ".repeat(index + 1))
                .append(it.toJsonSimplified(index + 1))
                .append("\n")
        })
        builder.append("  ".repeat(index)).append("]")
        return builder.toString()
    }

    override val size: Int
        get() = value.size

    override fun clear() {
        value.clear()
    }

    override fun addAll(elements: Collection<NBTTagData>): Boolean {
        return value.addAll(elements)
    }

    override fun addAll(index: Int, elements: Collection<NBTTagData>): Boolean {
        return value.addAll(index, elements)
    }

    override fun add(index: Int, element: NBTTagData) {
        value.add(index, element)
    }

    override fun add(element: NBTTagData): Boolean {
        return value.add(element)
    }

    override fun get(index: Int): NBTTagData {
        return value[index]
    }

    override fun isEmpty(): Boolean {
        return value.isEmpty()
    }

    override fun iterator(): MutableIterator<NBTTagData> {
        return value.iterator()
    }

    override fun listIterator(): MutableListIterator<NBTTagData> {
        return value.listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<NBTTagData> {
        return value.listIterator(index)
    }

    override fun removeAt(index: Int): NBTTagData {
        return value.removeAt(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<NBTTagData> {
        return value.subList(fromIndex, toIndex)
    }

    override fun set(index: Int, element: NBTTagData): NBTTagData {
        return value.set(index, element)
    }

    override fun retainAll(elements: Collection<NBTTagData>): Boolean {
        return value.retainAll(elements.toSet())
    }

    override fun removeAll(elements: Collection<NBTTagData>): Boolean {
        return value.removeAll(elements.toSet())
    }

    override fun remove(element: NBTTagData): Boolean {
        return value.remove(element)
    }

    override fun lastIndexOf(element: NBTTagData): Int {
        return value.lastIndexOf(element)
    }

    override fun indexOf(element: NBTTagData): Int {
        return value.indexOf(element)
    }

    override fun containsAll(elements: Collection<NBTTagData>): Boolean {
        return value.containsAll(elements)
    }

    override fun contains(element: NBTTagData): Boolean {
        return value.contains(element)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NBTTagList) return false
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return saveToString()
    }

    companion object {

        @JvmStatic
        fun of(vararg list: NBTTagData): NBTTagList {
            return NBTTagList(list.toList())
        }

        @JvmStatic
        fun of(vararg list: Any): NBTTagList {
            return NBTTagList(list.map { toNBT(it) })
        }
    }
}
package taboolib.module.nms

import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer

/**
 * TabooLib
 * taboolib.module.nms.ItemTagList
 *
 * @author 坏黑
 * @since 2023/8/10 01:17
 */
class ItemTagList : ItemTagData, MutableList<ItemTagData> {

    private val value = CopyOnWriteArrayList<ItemTagData>()

    constructor() : super(ItemTagType.LIST, 0) {
        this.data = this
    }

    constructor(list: List<ItemTagData>) : super(ItemTagType.LIST, 0) {
        this.data = this
        this.value += list
    }

    override fun asList(): ItemTagList {
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

    override fun addAll(elements: Collection<ItemTagData>): Boolean {
        return value.addAll(elements)
    }

    override fun addAll(index: Int, elements: Collection<ItemTagData>): Boolean {
        return value.addAll(index, elements)
    }

    override fun add(index: Int, element: ItemTagData) {
        value.add(index, element)
    }

    override fun add(element: ItemTagData): Boolean {
        return value.add(element)
    }

    override fun get(index: Int): ItemTagData {
        return value[index]
    }

    override fun isEmpty(): Boolean {
        return value.isEmpty()
    }

    override fun iterator(): MutableIterator<ItemTagData> {
        return value.iterator()
    }

    override fun listIterator(): MutableListIterator<ItemTagData> {
        return value.listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<ItemTagData> {
        return value.listIterator(index)
    }

    override fun removeAt(index: Int): ItemTagData {
        return value.removeAt(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<ItemTagData> {
        return value.subList(fromIndex, toIndex)
    }

    override fun set(index: Int, element: ItemTagData): ItemTagData {
        return value.set(index, element)
    }

    override fun retainAll(elements: Collection<ItemTagData>): Boolean {
        return value.retainAll(elements.toSet())
    }

    override fun removeAll(elements: Collection<ItemTagData>): Boolean {
        return value.removeAll(elements.toSet())
    }

    override fun remove(element: ItemTagData): Boolean {
        return value.remove(element)
    }

    override fun lastIndexOf(element: ItemTagData): Int {
        return value.lastIndexOf(element)
    }

    override fun indexOf(element: ItemTagData): Int {
        return value.indexOf(element)
    }

    override fun containsAll(elements: Collection<ItemTagData>): Boolean {
        return value.containsAll(elements)
    }

    override fun contains(element: ItemTagData): Boolean {
        return value.contains(element)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ItemTagList) return false
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
        fun of(vararg list: ItemTagData): ItemTagList {
            return ItemTagList(list.toList())
        }

        @JvmStatic
        fun of(vararg list: Any): ItemTagList {
            return ItemTagList(list.map { toNBT(it) })
        }
    }
}
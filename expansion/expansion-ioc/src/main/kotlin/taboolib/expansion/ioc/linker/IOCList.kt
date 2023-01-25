package taboolib.expansion.ioc.linker

import taboolib.expansion.ioc.IOCReader
import taboolib.expansion.ioc.IndexReader
import taboolib.expansion.ioc.database.IOCDatabase
import taboolib.expansion.ioc.database.impl.IOCDatabaseYaml
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.UnaryOperator
import java.util.stream.Stream

//创建一个IOCList 进行连接IOC容器
inline fun <reified T : Any?> linkedIOCList(): IOCList<T> {
    return IOCList(T::class.java)
}

class IOCList<E : Any?>(
    dataType: Class<*>,
) : ArrayList<E>() {

    val IOC by lazy {
        IOCReader.dataMap.getOrPut(dataType.name) { ConcurrentHashMap() }
    }

    val DATABASE by lazy{
        IOCReader.databaseMap.getOrPut(dataType.name) { IOCDatabaseYaml() }
    }

    /**
     * 向IOC容器中添加一个数据
     *
     * @param 添加的内容
     * @return 返回ture或者false
     */
    override fun add(element: E): Boolean {
        element?.let {
            IOC[IndexReader.getIndexId(it)] = it
        }
        return true
    }

    /**
     * 很抱歉，IOCList并不具有顺序 他的实现是一个链表
     * 没有排序这个功能
     * 但是我也会为你添加数据 来方便进行兼容
     */
    override fun add(index: Int, element: E) {
        element?.let {
            IOC[IndexReader.getIndexId(it)] = it
        }
    }

    /**
     * 添加所有的数据 会覆盖相同索引的数据
     * 请定义好索引
     */
    override fun addAll(elements: Collection<E>): Boolean {
        elements.forEach { data ->
            data?.let {
                IOC[IndexReader.getIndexId(it)] = it
            }
        }
        return true
    }

    /**
     * 很抱歉，IOCList并不具有顺序 他的实现是一个链表
     * 没有排序这个功能
     * 但是我也会为你添加数据 来方便进行兼容
     */
    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        elements.forEach { data ->
            data?.let {
                IOC[IndexReader.getIndexId(it)] = it
            }
        }
        return true
    }

    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    override fun clear() {
        IOC.clear()
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     */
    @Deprecated("这里并不适用 请使用iteratorIOC", ReplaceWith("iteratorIOC()"))
    override fun iterator(): MutableIterator<E> {
        error("Please do not use this method")
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     */
    fun iteratorIOC(): MutableIterator<Any> {
        return IOC.values.iterator()
    }

    /**
     * 在IOC容器中删除一个数据
     * 这个数据并不是以对象对比进行识别的 而是识别的索引ID
     * (就算你不指定一个索引 系统也会为你分配一个唯一索引)
     */
    override fun remove(element: E): Boolean {
        element?.let {
            IOC.remove(IndexReader.getIndexId(it))
        }
        return true
    }

    /**
     * 在IOC容器中删除多个数据
     * 这个数据并不是以对象对比进行识别的 而是识别的索引ID
     * (就算你不指定一个索引 系统也会为你分配一个唯一索引)
     */
    override fun removeAll(elements: Collection<E>): Boolean {
        elements.forEach { element ->
            element?.let {
                IOC.remove(IndexReader.getIndexId(it))
            }
        }
        return true
    }

    /**
     * 保留集合中的Data
     * 其余不符合的Data都会删除
     */
    override fun retainAll(elements: Collection<E>): Boolean {
        val ids = elements.mapNotNull {
            if (it == null) {
                null
            } else {
                IndexReader.getIndexId(it)
            }
        }
        IOC.toMap().forEach { t, u ->
            if (!ids.contains(t)) {
                IOC.remove(t)
            }
        }
        return true
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    override fun contains(element: E): Boolean {
        return IOC.containsKey(IndexReader.getIndexId(element ?: return false))
    }

    /**
     * {@inheritDoc}
     *
     *
     * This implementation iterates over the specified collection,
     * checking each element returned by the iterator in turn to see
     * if it's contained in this collection.  If all elements are so
     * contained <tt>true</tt> is returned, otherwise <tt>false</tt>.
     *
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @see .contains
     */
    override fun containsAll(elements: Collection<E>): Boolean {
        elements.filterNotNull().forEach {
            if (!IOC.containsKey(IndexReader.getIndexId(it))) {
                return false
            }
        }
        return true
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements
     */
    override fun isEmpty(): Boolean {
        return IOC.isEmpty()
    }

    /**
     * Compares the specified object with this list for equality.  Returns
     * `true` if and only if the specified object is also a list, both
     * lists have the same size, and all corresponding pairs of elements in
     * the two lists are *equal*.  (Two elements `e1` and
     * `e2` are *equal* if `(e1==null ? e2==null :
     * e1.equals(e2))`.)  In other words, two lists are defined to be
     * equal if they contain the same elements in the same order.
     *
     *
     *
     * This implementation first checks if the specified object is this
     * list. If so, it returns `true`; if not, it checks if the
     * specified object is a list. If not, it returns `false`; if so,
     * it iterates over both lists, comparing corresponding pairs of elements.
     * If any comparison returns `false`, this method returns
     * `false`.  If either iterator runs out of elements before the
     * other it returns `false` (as the lists are of unequal length);
     * otherwise it returns `true` when the iterations complete.
     *
     * @param o the object to be compared for equality with this list
     * @return `true` if the specified object is equal to this list
     */
    override fun equals(other: Any?): Boolean {
        return IOC.equals(other)
    }

    /**
     * Returns the hash code value for this list.
     *
     *
     * This implementation uses exactly the code that is used to define the
     * list hash function in the documentation for the [List.hashCode]
     * method.
     *
     * @return the hash code value for this list
     */
    override fun hashCode(): Int {
        return IOC.hashCode()
    }

    /**
     * Returns a string representation of this collection.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by [String.valueOf].
     *
     * @return a string representation of this collection
     */
    override fun toString(): String {
        return IOC.toString()
    }

    @Suppress("UNREACHABLE_CODE")
    @Deprecated("请不要用这个迭代方法", ReplaceWith("forEachIOC(action: Pair<String, Any>.() -> Unit)"))
    override fun forEach(action: Consumer<in E>) {
        error("不要用这个迭代方法")
    }

    /**
     * 遍历这个方法
     * first 是这个对象的ID
     * last 是这个对象的Data
     */
    fun forEachIOC(action: Pair<String, Any>.() -> Unit) {
        IOC.forEach { (t, u) ->
            action.invoke(t to u)
        }
    }

    /**
     * Creates a *[late-binding](Spliterator.html#binding)*
     * and *fail-fast* [Spliterator] over the elements in this
     * list.
     *
     *
     * The `Spliterator` reports [Spliterator.SIZED],
     * [Spliterator.SUBSIZED], and [Spliterator.ORDERED].
     * Overriding implementations should document the reporting of additional
     * characteristic values.
     *
     * @return a `Spliterator` over the elements in this list
     * @since 1.8
     */
    @Suppress("UNREACHABLE_CODE")
    override fun spliterator(): Spliterator<E> {
        error("无法使用这个方法")
    }

    /**
     * 返回一个由Data组成的Array
     * 但是此Array会失去对IOC的绑定
     */
    override fun toArray(): Array<Any> {
        return IOC.values.toTypedArray()
    }

    @Suppress("UNREACHABLE_CODE")
    override fun <T : Any?> toArray(a: Array<out T>): Array<T> {
        error("无法使用这个方法")
    }


    @Deprecated("请不要用这个迭代方法", ReplaceWith("removeIfIOC(filter: Pair<String, Any>.() -> Boolean): Boolean"))
    override fun removeIf(filter: Predicate<in E>): Boolean {
        error("无法使用这个方法")
    }

    /**
     * 会删除所有返回True的Data
     * 由数据删除返回true
     * 没有则返回false
     */
    fun removeIfIOC(filter: Pair<String, Any>.() -> Boolean): Boolean {
        var temp = false
        IOC.toMap().forEach { (t, u) ->
            if (filter.invoke(t to u)) {
                IOC.remove(t)
                temp = true
            }
        }
        return temp
    }

    /**
     * 替换成了Kt的流处理
     */
    @Deprecated("请不要用这个迭代方法", ReplaceWith("streamIOC()"))
    override fun stream(): Stream<E> {
        return super.stream()
    }

    /**
     * 创建一个流但是这会破坏IOC的数据绑定？
     * 不确定
     */
    fun streamIOC(): Sequence<Map.Entry<String, Any>> {
        return IOC.asSequence()
    }

    @Suppress("UNREACHABLE_CODE")
    override fun parallelStream(): Stream<E> {
        error("无法使用这个方法")
    }

    @Suppress("UNREACHABLE_CODE")
    override fun listIterator(index: Int): MutableListIterator<E> {
        error("无法使用这个方法")
    }

    @Suppress("UNREACHABLE_CODE")
    override fun listIterator(): MutableListIterator<E> {
        error("无法使用这个方法")
    }

    /**
     * IOC容器的实现是一个链表 无法删除第几个
     * 提供了删除某个ID的同名方法
     */
    @Suppress("UNREACHABLE_CODE")
    override fun removeAt(index: Int): E {
        error("无法使用这个方法")
    }

    fun removeAt(index: String): Any? {
        return IOC.remove(index)
    }

    /**
     * IOC容器的实现是一个链表 无法修改第几个
     *  提供了修改某个ID的同名方法
     */
    @Suppress("UNREACHABLE_CODE")
    override fun set(index: Int, element: E): E {
        error("无法使用这个方法")
    }

    fun set(index: String, element: Any): Any? {
        IOC[index] = element
        return IOC[index]
    }

    @Suppress("UNREACHABLE_CODE")
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        error("无法切割list")
    }

    /**
     * 因为链表的设计无法get第几个数据 但是提供了get某个ID的数据
     */
    @Suppress("UNREACHABLE_CODE")
    override fun get(index: Int): E {
        error("无法使用这个方法")
    }

    fun get(index: String): Any? {
        return IOC[index]
    }

    /**
     * IOC容器是链表结构 无法返回下标
     */
    @Suppress("UNREACHABLE_CODE")
    override fun indexOf(element: E): Int {
        error("无法使用这个方法")
    }

    /**
     * IOC容器是链表结构 无法返回下标
     */
    @Suppress("UNREACHABLE_CODE")
    override fun lastIndexOf(element: E): Int {
        error("无法使用这个方法")
    }

    @Suppress("UNREACHABLE_CODE")
    override fun replaceAll(operator: UnaryOperator<E>) {
        error("无法使用这个方法")
    }

    @Suppress("UNREACHABLE_CODE")
    override fun sort(c: Comparator<in E>?) {
        error("无法使用这个方法")
    }

    /**
     * Removes from this list all of the elements whose index is between
     * `fromIndex`, inclusive, and `toIndex`, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by `(toIndex - fromIndex)` elements.
     * (If `toIndex==fromIndex`, this operation has no effect.)
     *
     * @throws IndexOutOfBoundsException if `fromIndex` or
     * `toIndex` is out of range
     * (`fromIndex < 0 ||
     * fromIndex >= size() ||
     * toIndex > size() ||
     * toIndex < fromIndex`)
     */
    @Suppress("UNREACHABLE_CODE")
    override fun removeRange(fromIndex: Int, toIndex: Int) {
        error("无法使用这个方法")
    }

    /**
     * 复制一个本IOCList容器
     * 并且与IOC容器绑定
     */
    override fun clone(): Any {
        return this.clone()
    }

    @Suppress("UNREACHABLE_CODE")
    override fun trimToSize() {
        error("不允许使用此方法")
    }


    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    override val size: Int
        get() = IOC.size
}
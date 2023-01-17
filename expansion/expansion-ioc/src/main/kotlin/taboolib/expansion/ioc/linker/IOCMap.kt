package taboolib.expansion.ioc.linker

import taboolib.expansion.ioc.IOCReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.*
import java.util.function.Function

inline fun <reified V : Any?> linkedIOCMap(): ConcurrentHashMap<String, Any> {
    return IOCMap(V::class.java)
}

class IOCMap(dataType: Class<*>) : ConcurrentHashMap<String, Any>() {


    val IOC by lazy {
        IOCReader.dataMap.getOrPut(dataType.name) { ConcurrentHashMap() }
    }

    override fun clear() {
        IOC.clear()
    }

    override fun isEmpty(): Boolean {
        return IOC.isEmpty()
    }

    override fun equals(other: Any?): Boolean {
        return IOC.equals(other)
    }

    override fun hashCode(): Int {
        return IOC.hashCode()
    }

    override fun toString(): String {
        return IOC.toString()
    }

    override fun clone(): Any {
        return super.clone()
    }

    fun cloneIOC(): Map<String, Any> {
        return IOC.toMap()
    }

    override fun contains(value: Any?): Boolean {
        return IOC.contains(value)
    }

    override fun keys(): Enumeration<String> {
        return IOC.keys()
    }

    override fun elements(): Enumeration<Any> {
        return IOC.elements()
    }

    override fun mappingCount(): Long {
        return IOC.mappingCount()
    }

    override val values: MutableCollection<Any>
        get() = IOC.values
    override val entries: MutableSet<MutableMap.MutableEntry<String, Any>>
        get() = IOC.entries
    override val keys: KeySetView<String, Any>
        get() = IOC.keys
    override val size: Int
        get() = IOC.size

    override fun reduceEntriesToInt(
        parallelismThreshold: Long,
        transformer: ToIntFunction<MutableMap.MutableEntry<String, Any>>?,
        basis: Int,
        reducer: IntBinaryOperator?,
    ): Int {
        return IOC.reduceEntriesToInt(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceEntriesToLong(
        parallelismThreshold: Long,
        transformer: ToLongFunction<MutableMap.MutableEntry<String, Any>>?,
        basis: Long,
        reducer: LongBinaryOperator?,
    ): Long {
        return IOC.reduceEntriesToLong(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceEntriesToDouble(
        parallelismThreshold: Long,
        transformer: ToDoubleFunction<MutableMap.MutableEntry<String, Any>>?,
        basis: Double,
        reducer: DoubleBinaryOperator?,
    ): Double {
        return IOC.reduceEntriesToDouble(parallelismThreshold, transformer, basis, reducer)
    }

    override fun <U : Any?> reduceEntries(
        parallelismThreshold: Long,
        transformer: Function<MutableMap.MutableEntry<String, Any>, out U>?,
        reducer: BiFunction<in U, in U, out U>?,
    ): U {
        return IOC.reduceEntries(parallelismThreshold, transformer, reducer)
    }

    override fun reduceEntries(
        parallelismThreshold: Long,
        reducer: BiFunction<MutableMap.MutableEntry<String, Any>, MutableMap.MutableEntry<String, Any>, out MutableMap.MutableEntry<String, Any>>?,
    ): MutableMap.MutableEntry<String, Any> {
        return IOC.reduceEntries(parallelismThreshold, reducer)
    }

    override fun <U : Any?> searchEntries(
        parallelismThreshold: Long,
        searchFunction: Function<MutableMap.MutableEntry<String, Any>, out U>?,
    ): U {
        return IOC.searchEntries(parallelismThreshold, searchFunction)
    }

    override fun <U : Any?> forEachEntry(
        parallelismThreshold: Long,
        transformer: Function<MutableMap.MutableEntry<String, Any>, out U>?,
        action: Consumer<in U>?,
    ) {
        IOC.forEachEntry(parallelismThreshold, transformer, action)
    }

    override fun forEachEntry(parallelismThreshold: Long, action: Consumer<in MutableMap.MutableEntry<String, Any>>?) {
        IOC.forEachEntry(parallelismThreshold, action)
    }

    override fun reduceValuesToInt(
        parallelismThreshold: Long,
        transformer: ToIntFunction<in Any>?,
        basis: Int,
        reducer: IntBinaryOperator?,
    ): Int {
        return IOC.reduceValuesToInt(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceValuesToLong(
        parallelismThreshold: Long,
        transformer: ToLongFunction<in Any>?,
        basis: Long,
        reducer: LongBinaryOperator?,
    ): Long {
        return IOC.reduceValuesToLong(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceValuesToDouble(
        parallelismThreshold: Long,
        transformer: ToDoubleFunction<in Any>?,
        basis: Double,
        reducer: DoubleBinaryOperator?,
    ): Double {
        return IOC.reduceValuesToDouble(parallelismThreshold, transformer, basis, reducer)
    }

    override fun <U : Any?> reduceValues(
        parallelismThreshold: Long,
        transformer: Function<in Any, out U>?,
        reducer: BiFunction<in U, in U, out U>?,
    ): U {
        return IOC.reduceValues(parallelismThreshold, transformer, reducer)
    }

    override fun reduceValues(parallelismThreshold: Long, reducer: BiFunction<in Any, in Any, out Any>?): Any {
        return IOC.reduceValues(parallelismThreshold, reducer)
    }

    override fun <U : Any?> searchValues(parallelismThreshold: Long, searchFunction: Function<in Any, out U>?): U {
        return IOC.searchValues(parallelismThreshold, searchFunction)
    }

    override fun <U : Any?> forEachValue(
        parallelismThreshold: Long,
        transformer: Function<in Any, out U>?,
        action: Consumer<in U>?,
    ) {
        IOC.forEachValue(parallelismThreshold, transformer, action)
    }

    override fun forEachValue(parallelismThreshold: Long, action: Consumer<in Any>?) {
        IOC.forEachValue(parallelismThreshold, action)
    }

    override fun reduceKeysToInt(
        parallelismThreshold: Long,
        transformer: ToIntFunction<in String>?,
        basis: Int,
        reducer: IntBinaryOperator?,
    ): Int {
        return IOC.reduceKeysToInt(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceKeysToLong(
        parallelismThreshold: Long,
        transformer: ToLongFunction<in String>?,
        basis: Long,
        reducer: LongBinaryOperator?,
    ): Long {
        return IOC.reduceKeysToLong(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceKeysToDouble(
        parallelismThreshold: Long,
        transformer: ToDoubleFunction<in String>?,
        basis: Double,
        reducer: DoubleBinaryOperator?,
    ): Double {
        return IOC.reduceKeysToDouble(parallelismThreshold, transformer, basis, reducer)
    }

    override fun <U : Any?> reduceKeys(
        parallelismThreshold: Long,
        transformer: Function<in String, out U>?,
        reducer: BiFunction<in U, in U, out U>?,
    ): U {
        return IOC.reduceKeys(parallelismThreshold, transformer, reducer)
    }

    override fun reduceKeys(
        parallelismThreshold: Long,
        reducer: BiFunction<in String, in String, out String>?,
    ): String {
        return IOC.reduceKeys(parallelismThreshold, reducer)
    }

    override fun <U : Any?> searchKeys(parallelismThreshold: Long, searchFunction: Function<in String, out U>?): U {
        return IOC.searchKeys(parallelismThreshold, searchFunction)
    }

    override fun <U : Any?> forEachKey(
        parallelismThreshold: Long,
        transformer: Function<in String, out U>?,
        action: Consumer<in U>?,
    ) {
        IOC.forEachKey(parallelismThreshold, transformer, action)
    }

    override fun forEachKey(parallelismThreshold: Long, action: Consumer<in String>?) {
        IOC.forEachKey(parallelismThreshold, action)
    }

    override fun reduceToInt(
        parallelismThreshold: Long,
        transformer: ToIntBiFunction<in String, in Any>?,
        basis: Int,
        reducer: IntBinaryOperator?,
    ): Int {
        return IOC.reduceToInt(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceToLong(
        parallelismThreshold: Long,
        transformer: ToLongBiFunction<in String, in Any>?,
        basis: Long,
        reducer: LongBinaryOperator?,
    ): Long {
        return IOC.reduceToLong(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceToDouble(
        parallelismThreshold: Long,
        transformer: ToDoubleBiFunction<in String, in Any>?,
        basis: Double,
        reducer: DoubleBinaryOperator?,
    ): Double {
        return IOC.reduceToDouble(parallelismThreshold, transformer, basis, reducer)
    }

    override fun <U : Any?> reduce(
        parallelismThreshold: Long,
        transformer: BiFunction<in String, in Any, out U>?,
        reducer: BiFunction<in U, in U, out U>?,
    ): U {
        return IOC.reduce(parallelismThreshold, transformer, reducer)
    }

    override fun <U : Any?> search(
        parallelismThreshold: Long,
        searchFunction: BiFunction<in String, in Any, out U>?,
    ): U {
        return IOC.search(parallelismThreshold, searchFunction)
    }

    override fun merge(key: String, value: Any, remappingFunction: BiFunction<in Any, in Any, out Any?>): Any? {
        return IOC.merge(key, value, remappingFunction)
    }

    override fun compute(key: String, remappingFunction: BiFunction<in String, in Any?, out Any?>): Any? {
        return IOC.compute(key, remappingFunction)
    }

    override fun computeIfPresent(key: String, remappingFunction: BiFunction<in String, in Any, out Any?>): Any? {
        return IOC.computeIfPresent(key, remappingFunction)
    }

    override fun computeIfAbsent(key: String, mappingFunction: Function<in String, out Any>): Any {
        return IOC.computeIfAbsent(key, mappingFunction)
    }

    override fun replace(key: String, value: Any): Any? {
        return IOC.replace(key, value)
    }

    override fun replace(key: String, oldValue: Any, newValue: Any): Boolean {
        return IOC.replace(key, oldValue, newValue)
    }

    override fun putIfAbsent(key: String, value: Any): Any? {
        return IOC.putIfAbsent(key, value)
    }

    override fun replaceAll(function: BiFunction<in String, in Any, out Any>) {
        IOC.replaceAll(function)
    }

    override fun <U : Any?> forEach(
        parallelismThreshold: Long,
        transformer: BiFunction<in String, in Any, out U>?,
        action: Consumer<in U>?,
    ) {
        IOC.forEach(parallelismThreshold, transformer, action)
    }

    override fun forEach(parallelismThreshold: Long, action: BiConsumer<in String, in Any>?) {
        IOC.forEach(parallelismThreshold, action)
    }

    override fun forEach(action: BiConsumer<in String, in Any>) {
        IOC.forEach(action)
    }

    override fun keySet(mappedValue: Any?): KeySetView<String, Any> {
        return IOC.keySet(mappedValue)
    }

    override fun getOrDefault(key: String, defaultValue: Any): Any {
        return IOC.getOrDefault(key, defaultValue)
    }

    override fun get(key: String): Any? {
        return IOC.get(key)
    }

    override fun containsValue(value: Any): Boolean {
        return IOC.containsValue(value)
    }

    override fun containsKey(key: String): Boolean {
        return IOC.containsKey(key)
    }

    override fun remove(key: String, value: Any): Boolean {
        return IOC.remove(key, value)
    }

    override fun remove(key: String): Any? {
        return IOC.remove(key)
    }

    override fun putAll(from: Map<out String, Any>) {
        IOC.putAll(from)
    }

    override fun put(key: String, value: Any): Any? {
        return IOC.put(key, value)
    }

}
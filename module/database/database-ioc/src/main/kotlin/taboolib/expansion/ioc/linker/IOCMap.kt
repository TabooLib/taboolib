package taboolib.expansion.ioc.linker

import taboolib.common.util.unsafeLazy
import taboolib.expansion.ioc.IOCReader
import taboolib.expansion.ioc.database.IOCDatabase
import taboolib.expansion.ioc.database.impl.IOCDatabaseYaml
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.*
import java.util.function.Function

inline fun <reified V : Any?> linkedIOCMap(): IOCMap {
    return IOCMap(V::class.java)
}

class IOCMap(dataType: Class<*>) : ConcurrentHashMap<String, Any>() {

    val ioc: ConcurrentHashMap<String, Any> by unsafeLazy {
        IOCReader.dataMap.getOrPut(dataType.name) { ConcurrentHashMap() }
    }

    val database: IOCDatabase by unsafeLazy {
        IOCReader.databaseMap.getOrPut(dataType.name) { IOCDatabaseYaml() }
    }

    override fun clear() {
        ioc.clear()
    }

    override fun isEmpty(): Boolean {
        return ioc.isEmpty()
    }

    override fun equals(other: Any?): Boolean {
        return ioc.equals(other)
    }

    override fun hashCode(): Int {
        return ioc.hashCode()
    }

    override fun toString(): String {
        return ioc.toString()
    }

    override fun clone(): Any {
        return super.clone()
    }

    fun cloneIOC(): Map<String, Any> {
        return ioc.toMap()
    }

    override fun contains(value: Any?): Boolean {
        return ioc.contains(value)
    }

    override fun keys(): Enumeration<String> {
        return ioc.keys()
    }

    override fun elements(): Enumeration<Any> {
        return ioc.elements()
    }

    override fun mappingCount(): Long {
        return ioc.mappingCount()
    }

    override val values: MutableCollection<Any>
        get() = ioc.values

    override val entries: MutableSet<MutableMap.MutableEntry<String, Any>>
        get() = ioc.entries

    override val keys: KeySetView<String, Any>
        get() = ioc.keys

    override val size: Int
        get() = ioc.size

    override fun reduceEntriesToInt(
        parallelismThreshold: Long,
        transformer: ToIntFunction<MutableMap.MutableEntry<String, Any>>?,
        basis: Int,
        reducer: IntBinaryOperator?,
    ): Int {
        return ioc.reduceEntriesToInt(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceEntriesToLong(
        parallelismThreshold: Long,
        transformer: ToLongFunction<MutableMap.MutableEntry<String, Any>>?,
        basis: Long,
        reducer: LongBinaryOperator?,
    ): Long {
        return ioc.reduceEntriesToLong(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceEntriesToDouble(
        parallelismThreshold: Long,
        transformer: ToDoubleFunction<MutableMap.MutableEntry<String, Any>>?,
        basis: Double,
        reducer: DoubleBinaryOperator?,
    ): Double {
        return ioc.reduceEntriesToDouble(parallelismThreshold, transformer, basis, reducer)
    }

    override fun <U : Any?> reduceEntries(
        parallelismThreshold: Long,
        transformer: Function<MutableMap.MutableEntry<String, Any>, out U>?,
        reducer: BiFunction<in U, in U, out U>?,
    ): U {
        return ioc.reduceEntries(parallelismThreshold, transformer, reducer)
    }

    override fun reduceEntries(
        parallelismThreshold: Long,
        reducer: BiFunction<MutableMap.MutableEntry<String, Any>, MutableMap.MutableEntry<String, Any>, out MutableMap.MutableEntry<String, Any>>?,
    ): MutableMap.MutableEntry<String, Any> {
        return ioc.reduceEntries(parallelismThreshold, reducer)
    }

    override fun <U : Any?> searchEntries(
        parallelismThreshold: Long,
        searchFunction: Function<MutableMap.MutableEntry<String, Any>, out U>?,
    ): U {
        return ioc.searchEntries(parallelismThreshold, searchFunction)
    }

    override fun <U : Any?> forEachEntry(
        parallelismThreshold: Long,
        transformer: Function<MutableMap.MutableEntry<String, Any>, out U>?,
        action: Consumer<in U>?,
    ) {
        ioc.forEachEntry(parallelismThreshold, transformer, action)
    }

    override fun forEachEntry(parallelismThreshold: Long, action: Consumer<in MutableMap.MutableEntry<String, Any>>?) {
        ioc.forEachEntry(parallelismThreshold, action)
    }

    override fun reduceValuesToInt(
        parallelismThreshold: Long,
        transformer: ToIntFunction<in Any>?,
        basis: Int,
        reducer: IntBinaryOperator?,
    ): Int {
        return ioc.reduceValuesToInt(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceValuesToLong(
        parallelismThreshold: Long,
        transformer: ToLongFunction<in Any>?,
        basis: Long,
        reducer: LongBinaryOperator?,
    ): Long {
        return ioc.reduceValuesToLong(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceValuesToDouble(
        parallelismThreshold: Long,
        transformer: ToDoubleFunction<in Any>?,
        basis: Double,
        reducer: DoubleBinaryOperator?,
    ): Double {
        return ioc.reduceValuesToDouble(parallelismThreshold, transformer, basis, reducer)
    }

    override fun <U : Any?> reduceValues(
        parallelismThreshold: Long,
        transformer: Function<in Any, out U>?,
        reducer: BiFunction<in U, in U, out U>?,
    ): U {
        return ioc.reduceValues(parallelismThreshold, transformer, reducer)
    }

    override fun reduceValues(parallelismThreshold: Long, reducer: BiFunction<in Any, in Any, out Any>?): Any {
        return ioc.reduceValues(parallelismThreshold, reducer)
    }

    override fun <U : Any?> searchValues(parallelismThreshold: Long, searchFunction: Function<in Any, out U>?): U {
        return ioc.searchValues(parallelismThreshold, searchFunction)
    }

    override fun <U : Any?> forEachValue(
        parallelismThreshold: Long,
        transformer: Function<in Any, out U>?,
        action: Consumer<in U>?,
    ) {
        ioc.forEachValue(parallelismThreshold, transformer, action)
    }

    override fun forEachValue(parallelismThreshold: Long, action: Consumer<in Any>?) {
        ioc.forEachValue(parallelismThreshold, action)
    }

    override fun reduceKeysToInt(
        parallelismThreshold: Long,
        transformer: ToIntFunction<in String>?,
        basis: Int,
        reducer: IntBinaryOperator?,
    ): Int {
        return ioc.reduceKeysToInt(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceKeysToLong(
        parallelismThreshold: Long,
        transformer: ToLongFunction<in String>?,
        basis: Long,
        reducer: LongBinaryOperator?,
    ): Long {
        return ioc.reduceKeysToLong(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceKeysToDouble(
        parallelismThreshold: Long,
        transformer: ToDoubleFunction<in String>?,
        basis: Double,
        reducer: DoubleBinaryOperator?,
    ): Double {
        return ioc.reduceKeysToDouble(parallelismThreshold, transformer, basis, reducer)
    }

    override fun <U : Any?> reduceKeys(
        parallelismThreshold: Long,
        transformer: Function<in String, out U>?,
        reducer: BiFunction<in U, in U, out U>?,
    ): U {
        return ioc.reduceKeys(parallelismThreshold, transformer, reducer)
    }

    override fun reduceKeys(
        parallelismThreshold: Long,
        reducer: BiFunction<in String, in String, out String>?,
    ): String {
        return ioc.reduceKeys(parallelismThreshold, reducer)
    }

    override fun <U : Any?> searchKeys(parallelismThreshold: Long, searchFunction: Function<in String, out U>?): U {
        return ioc.searchKeys(parallelismThreshold, searchFunction)
    }

    override fun <U : Any?> forEachKey(
        parallelismThreshold: Long,
        transformer: Function<in String, out U>?,
        action: Consumer<in U>?,
    ) {
        ioc.forEachKey(parallelismThreshold, transformer, action)
    }

    override fun forEachKey(parallelismThreshold: Long, action: Consumer<in String>?) {
        ioc.forEachKey(parallelismThreshold, action)
    }

    override fun reduceToInt(
        parallelismThreshold: Long,
        transformer: ToIntBiFunction<in String, in Any>?,
        basis: Int,
        reducer: IntBinaryOperator?,
    ): Int {
        return ioc.reduceToInt(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceToLong(
        parallelismThreshold: Long,
        transformer: ToLongBiFunction<in String, in Any>?,
        basis: Long,
        reducer: LongBinaryOperator?,
    ): Long {
        return ioc.reduceToLong(parallelismThreshold, transformer, basis, reducer)
    }

    override fun reduceToDouble(
        parallelismThreshold: Long,
        transformer: ToDoubleBiFunction<in String, in Any>?,
        basis: Double,
        reducer: DoubleBinaryOperator?,
    ): Double {
        return ioc.reduceToDouble(parallelismThreshold, transformer, basis, reducer)
    }

    override fun <U : Any?> reduce(
        parallelismThreshold: Long,
        transformer: BiFunction<in String, in Any, out U>?,
        reducer: BiFunction<in U, in U, out U>?,
    ): U {
        return ioc.reduce(parallelismThreshold, transformer, reducer)
    }

    override fun <U : Any?> search(
        parallelismThreshold: Long,
        searchFunction: BiFunction<in String, in Any, out U>?,
    ): U {
        return ioc.search(parallelismThreshold, searchFunction)
    }

    override fun merge(key: String, value: Any, remappingFunction: BiFunction<in Any, in Any, out Any?>): Any? {
        return ioc.merge(key, value, remappingFunction)
    }

    override fun compute(key: String, remappingFunction: BiFunction<in String, in Any?, out Any?>): Any? {
        return ioc.compute(key, remappingFunction)
    }

    override fun computeIfPresent(key: String, remappingFunction: BiFunction<in String, in Any, out Any?>): Any? {
        return ioc.computeIfPresent(key, remappingFunction)
    }

    override fun computeIfAbsent(key: String, mappingFunction: Function<in String, out Any>): Any {
        return ioc.computeIfAbsent(key, mappingFunction)
    }

    override fun replace(key: String, value: Any): Any? {
        return ioc.replace(key, value)
    }

    override fun replace(key: String, oldValue: Any, newValue: Any): Boolean {
        return ioc.replace(key, oldValue, newValue)
    }

    override fun putIfAbsent(key: String, value: Any): Any? {
        return ioc.putIfAbsent(key, value)
    }

    override fun replaceAll(function: BiFunction<in String, in Any, out Any>) {
        ioc.replaceAll(function)
    }

    override fun <U : Any?> forEach(
        parallelismThreshold: Long,
        transformer: BiFunction<in String, in Any, out U>?,
        action: Consumer<in U>?,
    ) {
        ioc.forEach(parallelismThreshold, transformer, action)
    }

    override fun forEach(parallelismThreshold: Long, action: BiConsumer<in String, in Any>?) {
        ioc.forEach(parallelismThreshold, action)
    }

    override fun forEach(action: BiConsumer<in String, in Any>) {
        ioc.forEach(action)
    }

    override fun keySet(mappedValue: Any?): KeySetView<String, Any> {
        return ioc.keySet(mappedValue)
    }

    override fun getOrDefault(key: String, defaultValue: Any): Any {
        return ioc.getOrDefault(key, defaultValue)
    }

    override fun get(key: String): Any? {
        return ioc.get(key)
    }

    override fun containsValue(value: Any): Boolean {
        return ioc.containsValue(value)
    }

    override fun containsKey(key: String): Boolean {
        return ioc.containsKey(key)
    }

    override fun remove(key: String, value: Any): Boolean {
        return ioc.remove(key, value)
    }

    override fun remove(key: String): Any? {
        return ioc.remove(key)
    }

    override fun putAll(from: Map<out String, Any>) {
        ioc.putAll(from)
    }

    override fun put(key: String, value: Any): Any? {
        return ioc.put(key, value)
    }
}
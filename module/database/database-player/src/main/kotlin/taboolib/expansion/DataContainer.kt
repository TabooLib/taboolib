package taboolib.expansion

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.submitAsync
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 缓存优先容器 Cache-First
 * 用于缓存数据并在一定时间后写入数据库
 * 数据库数据不同步给缓存
 *
 * @property user 用户标识
 * @property database 数据库实例
 */
@Suppress("DEPRECATION")
class DataContainer(val user: String, val database: Database) {

    /** 存储用户数据的源 */
    val source = database[user]

    /**
     * 待写入键的时间标记。
     *
     * **语义已变更**：早期实现存入的是 `当前时间 - 延迟`（即一个恒已过期的时间点），
     * 判断「是否该写库」需要检查它是否早于当前时间；
     * 现在存入的是**未来的 deadline**，即到达该时间点后才写库，判断条件正好相反。
     *
     * 该字段仅作为内部 [writeStates] 的影子副本保留以兼容既有读取方，
     * 请改用 [setDelayed] 表达延迟写入意图，不要依赖此字段做判断。
     */
    @Deprecated("语义已由「已过期时间」变更为「未来 deadline」，请勿依赖该字段做判断")
    val updateMap = ConcurrentHashMap<String, Long>()

    private val writeStates = ConcurrentHashMap<String, WriteState>()

    /**
     * 当前存在延迟期限的键集合。
     *
     * [checkUpdate] 每 tick 在主线程执行，只遍历该集合即可，
     * 避免随着容器写入的键增多而退化为 O(全部键) 的同步扫描。
     */
    private val deadlineKeys = ConcurrentHashMap.newKeySet<String>()

    internal var asyncExecutor: ((() -> Unit) -> Unit) = { task ->
        submitAsync { task() }
    }

    /**
     * 设置指定键的值并立即保存
     *
     * @param key 键
     * @param value 值
     */
    operator fun set(key: String, value: Any) {
        val stringValue = value.toString()
        updateValue(key, stringValue.takeUnless { it.isEmpty() }, deadline = null, updateSource = true)
    }

    /**
     * 穿透缓存的写数据库方法
     *
     * @param targetUser 目标用户
     * @param key 键
     * @param value 值
     * @param sync 是否同步给内存，要求targetUser为UUID
     */
    fun forcedSet(targetUser: String, key: String, value: Any, sync: Boolean = false) {
        val stringValue = value.toString()
        database[targetUser, key] = stringValue
        if (sync) {
            runCatching { UUID.fromString(targetUser) }.getOrNull()?.let { uniqueId ->
                // 数据库已在上一行写过，这里只同步内存缓存，避免重复排程一次写库
                playerDataContainer[uniqueId]?.setCacheOnly(key, stringValue)
            }
        }
    }

    /**
     * 仅同步内存缓存，不排程写库。
     *
     * 用于调用方已自行完成数据库写入的场景（例如 [forcedSet]），
     * 避免同一值写两遍，也避免在关服阶段因调度器拒绝任务而抛出异常。
     *
     * @param key 键
     * @param value 值，为空字符串时表示移除缓存
     */
    internal fun setCacheOnly(key: String, value: String) {
        withState(key) { state ->
            val newValue = value.takeUnless { it.isEmpty() }
            if (newValue == null) {
                source.remove(key)
            } else {
                source[key] = newValue
            }
            // 同步待写值并提升版本号，使已在队列中的旧快照不会把过期数据写回数据库
            state.revision++
            state.value = newValue
        }
    }

    /**
     * 设置指定键的值，并在指定延迟后更新
     *
     * @param key 键
     * @param value 值
     * @param delay 延迟时间
     * @param timeUnit 时间单位
     */
    fun setDelayed(key: String, value: Any, delay: Long = 3L, timeUnit: TimeUnit = TimeUnit.SECONDS) {
        val stringValue = value.toString()
        val deadline = deadlineAfter(timeUnit.toMillis(delay))
        updateValue(key, stringValue.takeUnless { it.isEmpty() }, deadline, updateSource = true)
    }

    /**
     * 获取指定键的值
     *
     * @param key 键
     * @return 对应的值，如果不存在则返回 null
     */
    operator fun get(key: String): String? {
        return source[key]
    }

    /**
     * 获取所有键的集合
     *
     * @return 键的集合
     */
    fun keys(): Set<String> {
        return source.keys
    }

    /**
     * 获取所有键值对
     *
     * @return 键值对映射
     */
    fun values(): Map<String, String> {
        return source
    }

    /**
     * 获取键值对的数量
     *
     * @return 键值对的数量
     */
    fun size(): Int {
        return source.size
    }

    /**
     * 保存指定键的值到数据库。
     *
     * 注意：**若该键不存在于缓存中，将删除数据库中对应的行**。
     * 早期实现在这种情况下会抛出 NullPointerException，现改为按「缓存即真相」处理，
     * 与 [set] / [delete] 走同一条写入路径。
     *
     * @param key 键
     */
    fun save(key: String) {
        withState(key) { state ->
            state.revision++
            state.value = source[key]
            state.deadline = null
            state.ready = true
            deadlineKeys.remove(key)
            updateMap.remove(key)
            if (state.startIfNeeded()) {
                scheduleWrite(key, state)
            }
        }
    }

    /**
     * 从数据库执行删除指定的键操作
     */
    fun delete(key: String) {
        updateValue(key, value = null, deadline = null, updateSource = false)
    }

    /**
     * 检查并更新需要保存的键值对
     */
    fun checkUpdate() {
        if (deadlineKeys.isEmpty()) {
            return
        }
        val currentTime = System.currentTimeMillis()
        deadlineKeys.toList().forEach { key ->
            val state = writeStates[key] ?: run {
                deadlineKeys.remove(key)
                return@forEach
            }
            synchronized(state) {
                val deadline = state.deadline
                if (deadline == null) {
                    deadlineKeys.remove(key)
                } else if (deadline <= currentTime) {
                    state.deadline = null
                    state.ready = true
                    deadlineKeys.remove(key)
                    updateMap.remove(key, deadline)
                    if (state.startIfNeeded()) {
                        scheduleWrite(key, state)
                    }
                }
            }
        }
    }

    /**
     * 立即排空所有未落库的写入。
     *
     * 将所有处于延迟期限内的键立即置为可写，并在**当前线程同步**完成写库。
     * 释放容器或插件关闭时必须调用，此时调度器可能已经拒绝新任务，
     * 因此这里不走 [asyncExecutor]，而是直接同步排空。
     */
    fun flush() {
        // 先把所有仍在延迟期限内的键置为可写
        writeStates.forEach { (key, state) ->
            synchronized(state) {
                if (state.deadline != null) {
                    state.deadline = null
                    state.ready = true
                    deadlineKeys.remove(key)
                    updateMap.remove(key)
                }
            }
        }
        // 再同步排空所有待写入的键，单个键失败不影响其余键
        // 若某个键已有排空任务在运行（running），则交由该任务完成，避免并发排空导致写入乱序
        var failure: Throwable? = null
        writeStates.forEach { (key, state) ->
            val shouldDrain = synchronized(state) { state.startIfNeeded() }
            if (!shouldDrain) {
                return@forEach
            }
            try {
                drainWrites(key, state)
            } catch (ex: Throwable) {
                val firstFailure = failure
                if (firstFailure == null) {
                    failure = ex
                } else {
                    firstFailure.addSuppressed(ex)
                }
            }
        }
        failure?.let { throw it }
    }

    private fun updateValue(key: String, value: String?, deadline: Long?, updateSource: Boolean) {
        withState(key) { state ->
            if (updateSource) {
                if (value == null) {
                    source.remove(key)
                } else {
                    source[key] = value
                }
            }
            state.revision++
            state.value = value
            state.deadline = deadline
            state.ready = deadline == null
            if (deadline == null) {
                deadlineKeys.remove(key)
                updateMap.remove(key)
            } else {
                deadlineKeys += key
                updateMap[key] = deadline
            }
            if (state.startIfNeeded()) {
                scheduleWrite(key, state)
            }
        }
    }

    /**
     * 获取指定键的写入状态并在其锁内执行操作。
     *
     * 写入状态在空闲时会被 [recycleState] 回收，因此这里必须循环校验取到的状态
     * 仍是映射中的当前实例，避免两个线程各自持有一个已被替换的状态对象。
     */
    private inline fun withState(key: String, block: (WriteState) -> Unit) {
        while (true) {
            val state = writeStates.computeIfAbsent(key) { WriteState() }
            val applied = synchronized(state) {
                if (state.discarded) {
                    false
                } else {
                    block(state)
                    true
                }
            }
            if (applied) {
                return
            }
        }
    }

    /**
     * 回收空闲的写入状态，避免 [writeStates] 只增不减。
     *
     * 必须在持有 [state] 锁时调用，且仅在状态确实空闲
     * （无延迟期限、无待写标记、无运行中的排空）时移除。
     */
    private fun recycleState(key: String, state: WriteState) {
        if (state.deadline != null || state.ready || state.running) {
            return
        }
        if (writeStates.remove(key, state)) {
            state.discarded = true
        }
    }

    private fun scheduleWrite(key: String, state: WriteState) {
        try {
            asyncExecutor.invoke {
                drainWrites(key, state)
            }
        } catch (ex: Throwable) {
            synchronized(state) {
                state.running = false
            }
            throw ex
        }
    }

    private fun drainWrites(key: String, state: WriteState) {
        while (true) {
            val snapshot = synchronized(state) {
                if (!state.ready) {
                    state.running = false
                    recycleState(key, state)
                    return
                }
                WriteSnapshot(state.revision, state.value)
            }
            try {
                if (snapshot.value == null) {
                    database.remove(user, key)
                } else {
                    database[user, key] = snapshot.value
                }
            } catch (ex: Throwable) {
                synchronized(state) {
                    val hasNewerValue = state.revision != snapshot.revision && state.ready
                    state.running = false
                    if (hasNewerValue) {
                        state.running = true
                        runCatching { scheduleWrite(key, state) }.exceptionOrNull()?.let(ex::addSuppressed)
                    }
                }
                throw ex
            }
            val shouldContinue = synchronized(state) {
                when {
                    state.revision == snapshot.revision -> {
                        state.ready = false
                        state.running = false
                        recycleState(key, state)
                        false
                    }
                    state.ready -> true
                    else -> {
                        state.running = false
                        recycleState(key, state)
                        false
                    }
                }
            }
            if (!shouldContinue) {
                return
            }
        }
    }

    private fun deadlineAfter(delayMillis: Long): Long {
        val currentTime = System.currentTimeMillis()
        return if (delayMillis > 0 && currentTime > Long.MAX_VALUE - delayMillis) {
            Long.MAX_VALUE
        } else {
            currentTime + delayMillis
        }
    }

    /**
     * 返回对象的字符串表示
     *
     * @return 对象的字符串表示
     */
    override fun toString(): String {
        return "DataContainer(user='$user', source=$source)"
    }

    private class WriteState {

        var revision = 0L
        var value: String? = null
        var deadline: Long? = null
        var ready = false
        var running = false

        /** 该状态是否已从 writeStates 中回收，回收后不得再被写入 */
        var discarded = false

        fun startIfNeeded(): Boolean {
            return if (ready && !running) {
                running = true
                true
            } else {
                false
            }
        }
    }

    private data class WriteSnapshot(val revision: Long, val value: String?)

    /**
     * 内部伴生对象，用于定期检查更新
     */
    @Inject
    internal companion object {

        /**
         * 定期检查并更新所有 DataContainer 实例
         */
        @Schedule(period = 20)
        fun checkUpdate() {
            playerDataContainer.entries.forEach { it.value.checkUpdate() }
        }

        /**
         * 插件关闭时排空所有容器中未落库的写入。
         *
         * 此时调度器可能已经拒绝新任务，[DataContainer.flush] 走同步路径，
         * 单个容器失败不影响其余容器。
         */
        @Awake(LifeCycle.DISABLE)
        fun flushAll() {
            playerDataContainer.values.forEach { container ->
                runCatching { container.flush() }.exceptionOrNull()?.let {
                    PrimitiveIO.warning("Failed to flush player data container {0}: {1}", container.user, it.toString())
                }
            }
        }
    }
}

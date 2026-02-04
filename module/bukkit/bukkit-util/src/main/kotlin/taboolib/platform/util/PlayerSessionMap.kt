package taboolib.platform.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.Inject
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.runSync
import java.io.Closeable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.LockSupport

/**
 * 维护以玩家 UUID 为键的会话对象。
 *
 * 每位玩家进入服务器都会分配一个递增的会话代（epoch），离线或被踢时该代会被移除，所有注册的容器会同步清除旧条目。
 * 任何与当前代不一致的值都会被视为过期，从而避免异步任务在玩家离线后继续创建或复活旧会话。
 * 过期条目通过延迟标记 + 定时清扫的策略批量移除，降低临界期内的哈希表抖动。
 * 若值实现 [PlayerSessionClosable]，条目被移除或替换时会回调，方便业务层释放资源。
 *
 * @param defaultFactory 默认工厂函数，用于 [getOrCreate] 无参版本
 * @param manualRelease 是否开启手动释放模式。为 `true` 时，玩家退出不会自动清理会话，需要手动调用 [remove] 释放
 */
@PlatformSide(Platform.BUKKIT)
class PlayerSessionMap<V : Any>(
    private val defaultFactory: ((UUID) -> V)? = null,
    private val manualRelease: Boolean = false,
) : Closeable {

    private val store = ConcurrentHashMap<UUID, SessionEntry<V>>()
    private val cleanupRequested = AtomicBoolean(false)

    init {
        PlayerSessionLifecycle.register(this)
    }

    /**
     * 基于玩家实体写入或替换会话对象。
     * 内部委托给 [set]。
     *
     * @param player 玩家实体
     * @param value 会话对象
     * @return 旧会话对象（若存在）
     */
    operator fun set(player: Player, value: V): V? = set(player.uniqueId, value)

    /**
     * 主动写入或替换指定玩家的会话对象。
     *
     * 仅当玩家当前仍在线时才会成功写入，并返回旧值（若存在）。
     * 若传入的值实现 [PlayerSessionClosable]，在旧值被替换或后续移除时会收到回调。
     *
     * @param uuid 玩家 UUID
     * @param value 会话对象
     * @return 旧会话对象（若存在）
     */
    operator fun set(uuid: UUID, value: V): V? {
        val epoch = PlayerSessionLifecycle.currentEpoch(uuid) ?: return null
        val newEntry = SessionEntry(epoch, value)
        var previous: SessionEntry<V>? = null
        store.compute(uuid) { _, old ->
            previous = old
            newEntry
        }
        previous?.invokeRemovalCallback(uuid)
        return previous?.value
    }

    /**
     * 获取指定玩家 UUID 对应的会话对象；
     * 若玩家不在场或会话已过期，则返回 `null`。
     *
     * @param uuid 玩家 UUID
     * @return 会话对象（若存在）
     */
    operator fun get(uuid: UUID): V? {
        val epoch = PlayerSessionLifecycle.currentEpoch(uuid) ?: return null
        return store[uuid]?.takeIf { it.epoch == epoch && !it.pendingRemoval }?.value
    }

    /**
     * 获取指定玩家实体对应的会话对象。
     * 内部委托给 [get]。
     *
     * @param player 玩家实体
     * @return 会话对象（若存在）
     */
    operator fun get(player: Player): V? = get(player.uniqueId)

    /**
     * 基于默认工厂获取或创建玩家会话。
     * 若未提供默认工厂，则返回 `null` 提示调用者使用其他重载。
     * 内部委托给 [getOrCreate]。
     *
     * @param player 玩家实体
     * @return 会话对象（若存在）
     */
    fun getOrCreate(player: Player): V? {
        val factory = defaultFactory ?: return null
        return getOrCreate(player.uniqueId) { factory(player.uniqueId) }
    }

    /**
     * 使用自定义供应函数获取或创建玩家会话。
     * 内部委托给 [getOrCreate]。
     *
     * @param player 玩家实体
     * @param supplier 会话对象供应函数
     * @return 会话对象（若存在）
     */
    fun getOrCreate(player: Player, supplier: () -> V): V? {
        return getOrCreate(player.uniqueId, supplier)
    }

    /**
     * 使用自定义供应函数获取或创建玩家会话。
     *
     * 创建过程中会先读取当前会话代；若玩家已离线则直接返回 `null`。
     * 此外会再检查一次 [PlayerSessionLifecycle.isOnline] 与最新会话代，防止事件顺序不同步时旧任务重新构造离线会话。
     *
     * 为兼顾正确性与性能，采用有界重试：
     * - 若在构建后检测到会话代发生变化，会在限定次数内重试，确保供应函数不会无限执行；
     * - 重试次数内仍无法获得稳定会话则返回 `null`，交由上层稍后重试，并在后期循环中加入轻量让步以避免忙等。
     *
     * @param uuid 玩家 UUID
     * @param supplier 会话对象供应函数
     * @return 会话对象（若存在）
     */
    fun getOrCreate(uuid: UUID, supplier: () -> V): V? {
        var cached: V? = null
        repeat(MAX_RETRY) { attempt ->
            val epoch = PlayerSessionLifecycle.currentEpoch(uuid) ?: return null
            val entry = store.compute(uuid) { _, old ->
                when {
                    // 会话有效，直接返回旧值   
                    old != null && old.epoch == epoch && !old.pendingRemoval -> old
                    // 玩家已离线：只做软删除（返回 old 保持键存在），后续访问会因为 pendingRemoval 被过滤，
                    // 并交由定时清扫统一 remove，避免在 compute 热点路径上立刻触发 HashMap 结构修改。
                    !PlayerSessionLifecycle.isOnline(uuid) -> {
                        if (old?.markForRemoval() == true) {
                            old.invokeRemovalCallback(uuid)
                            flagCleanup()
                        }
                        old
                    }

                    else -> {
                        old?.invokeRemovalCallback(uuid)
                        val value = cached ?: supplier().also { cached = it }
                        SessionEntry(epoch, value)
                    }
                }
            } ?: return null
            val latestEpoch = PlayerSessionLifecycle.currentEpoch(uuid)
            if (latestEpoch == epoch && entry.epoch == epoch && !entry.pendingRemoval) {
                return entry.value
            }
            if (attempt >= YIELD_AFTER) {
                LockSupport.parkNanos(1_000_000) // ≈1ms，小幅退避，避免 CPU 忙等
            }
        }
        return null
    }

    /**
     * 主动移除指定 UUID 对应的会话数据。
     *
     * @param uuid 玩家 UUID
     * @param invokeRemovalCallback 是否调用移除回调
     * @return 会话对象（若存在）
     */
    fun remove(uuid: UUID, invokeRemovalCallback: Boolean = true): V? {
        return store.remove(uuid)?.also { if (invokeRemovalCallback) it.invokeRemovalCallback(uuid) }?.value
    }

    /**
     * 主动移除指定玩家实体对应的会话数据。
     *
     * @param player 玩家实体
     * @param invokeRemovalCallback 是否调用移除回调
     * @return 会话对象（若存在）
     */
    fun remove(player: Player, invokeRemovalCallback: Boolean = true): V? {
        return remove(player.uniqueId, invokeRemovalCallback)
    }

    /**
     * 判断指定玩家实体是否拥有有效会话。
     * 内部委托给 [contains]。
     *
     * @param player 玩家实体
     * @return 是否存在有效会话
     */
    fun contains(player: Player): Boolean = contains(player.uniqueId)

    /**
     * 判断指定玩家是否拥有有效会话。
     *
     * @param uuid 玩家 UUID
     * @return 是否存在有效会话
     */
    fun contains(uuid: UUID): Boolean {
        val epoch = PlayerSessionLifecycle.currentEpoch(uuid) ?: return false
        return store[uuid]?.let { it.epoch == epoch && !it.pendingRemoval } ?: false
    }

    /**
     * 返回当前有效会话的惰性序列，只包含仍在线且会话代匹配的条目。
     */
    fun entries(): Sequence<Pair<UUID, V>> = store.entries.asSequence().mapNotNull { (uuid, entry) ->
        val epoch = PlayerSessionLifecycle.currentEpoch(uuid)
        if (epoch != null && entry.epoch == epoch && !entry.pendingRemoval) {
            uuid to entry.value
        } else {
            null
        }
    }

    /**
     * 返回当前有效会话 key 的惰性序列。
     */
    fun keys(): Sequence<UUID> = store.entries.asSequence().mapNotNull { (uuid, entry) ->
        val epoch = PlayerSessionLifecycle.currentEpoch(uuid)
        if (epoch != null && entry.epoch == epoch && !entry.pendingRemoval) uuid else null
    }

    /**
     * 返回当前有效会话值的惰性序列，只包含仍在线且会话代匹配的条目。
     */
    fun values(): Sequence<V> = store.entries.asSequence().mapNotNull { (uuid, entry) ->
        val epoch = PlayerSessionLifecycle.currentEpoch(uuid)
        if (epoch != null && entry.epoch == epoch && !entry.pendingRemoval) entry.value else null
    }

    /**
     * 返回当前有效会话数量。
     */
    fun size(): Int {
        var count = 0
        store.forEach { (uuid, entry) ->
            val epoch = PlayerSessionLifecycle.currentEpoch(uuid)
            if (epoch != null && entry.epoch == epoch && !entry.pendingRemoval) {
                count++
            }
        }
        return count
    }

    /**
     * 遍历当前所有有效会话。
     */
    fun forEach(action: (uuid: UUID, value: V) -> Unit) {
        store.forEach { (uuid, entry) ->
            val epoch = PlayerSessionLifecycle.currentEpoch(uuid)
            if (epoch != null && entry.epoch == epoch && !entry.pendingRemoval) {
                action(uuid, entry.value)
            }
        }
    }

    /**
     * 清空容器中维护的所有会话。
     */
    fun clear() {
        store.entries.forEach { (uuid, entry) -> entry.invokeRemovalCallback(uuid) }
        store.clear()
        cleanupRequested.set(false)
    }

    /**
     * 取消会话容器的生命周期注册，并清空内部数据。
     */
    override fun close() {
        PlayerSessionLifecycle.unregister(this)
        clear()
    }

    /**
     * 不安全获取
     * 不会检查会话是否过期
     */
    fun unsafeGet(uuid: UUID): V? {
        return store[uuid]?.value
    }

    /**
     * 不安全获取
     * 不会检查会话是否过期
     */
    fun unsafeGet(player: Player): V? {
        return unsafeGet(player.uniqueId)
    }

    /**
     * 供生命周期管理器调用，在会话代失效时删除对应条目。
     * 如果开启了手动释放模式，此方法不会执行任何操作。
     */
    internal fun invalidate(uuid: UUID, epoch: Long) {
        if (manualRelease) return
        store.computeIfPresent(uuid) { _, entry ->
            if (entry.epoch == epoch && entry.markForRemoval()) {
                entry.invokeRemovalCallback(uuid)
                flagCleanup()
            }
            entry
        }
    }

    internal fun cleanupPending() {
        if (!cleanupRequested.compareAndSet(true, false)) {
            return
        }
        store.entries.removeIf { (uuid, entry) ->
            if (entry.pendingRemoval || PlayerSessionLifecycle.currentEpoch(uuid)?.let { it != entry.epoch } ?: true) {
                entry.invokeRemovalCallback(uuid)
                true
            } else {
                false
            }
        }
    }

    private fun flagCleanup() {
        cleanupRequested.set(true)
    }

    private companion object {
        // 最大重试次数，用于处理会话代不一致的情况（限制供应函数重复执行次数）
        const val MAX_RETRY = 4

        // 触发让步的重试次数，用于平衡性能与正确性
        const val YIELD_AFTER = 1
    }
}

/**
 * 玩家会话条目，仅存储会话代与实际值。
 */
class SessionEntry<V : Any>(val epoch: Long, val value: V) {

    @Volatile
    var pendingRemoval: Boolean = false
        private set

    fun markForRemoval(): Boolean {
        if (!pendingRemoval) {
            pendingRemoval = true
            return true
        }
        return false
    }

    val removalCallbackInvoked = AtomicBoolean(false)

    fun invokeRemovalCallback(uuid: UUID) {
        if (removalCallbackInvoked.compareAndSet(false, true)) {
            if (value is PlayerSessionClosable) {
                value.onSessionRemove(uuid)
            }
        }
    }
}

fun interface PlayerSessionClosable {

    /**
     * 当玩家会话条目被移除、替换或清理时触发。
     * 注意：可能在异步线程调用，如需主线程操作请自行调度。
     */
    fun onSessionRemove(uuid: UUID)
}

/**
 * 玩家会话生命周期管理器，负责维护会话代，并在玩家上线/离线时通知所有注册的容器。
 * 为了兼顾性能，只在至少存在一个 [PlayerSessionMap] 时才开启代管理逻辑；
 * 同时使用定时任务统一回收各容器中标记为过期的会话，避免在热点路径上频繁结构性修改。
 */
@Awake
@Inject
object PlayerSessionLifecycle {

    // 服务器是否已启动
    private var isServerStarted = false

    // 是否已启用会话代管理
    private val isEnabled = AtomicBoolean(false)

    // 已登录玩家 UUID 集
    private val onlinePlayers = ConcurrentHashMap.newKeySet<UUID>()

    val maps = CopyOnWriteArraySet<PlayerSessionMap<*>>()
    val epochs = ConcurrentHashMap<UUID, Long>()
    val epochCounter = AtomicLong(0)

    /**
     * 注册一个会话容器，使其能接收生命周期回调。首次注册会启用生命周期管理并同步当前在线玩家。
     */
    fun register(map: PlayerSessionMap<*>) {
        maps += map
        if (isEnabled.compareAndSet(false, true)) {
            initializeOnlinePlayers()
        }
    }

    /**
     * 取消注册会话容器。若已经没有容器，则主动关闭生命周期管理并回收所有会话代信息。
     */
    fun unregister(map: PlayerSessionMap<*>) {
        maps -= map
        if (maps.isEmpty() && isEnabled.compareAndSet(true, false)) {
            epochs.clear()
            onlinePlayers.clear()
        }
    }

    /**
     * 查询玩家当前的会话代，若玩家不在场或尚未建立会话则返回 `null`。
     */
    fun currentEpoch(uuid: UUID): Long? = epochs[uuid]

    /**
     * 查询生命周期记录下的在线状态，避免在异步场景直接访问 Bukkit。
     */
    fun isOnline(uuid: UUID): Boolean {
        return isEnabled.get() && onlinePlayers.contains(uuid)
    }

    @Schedule(period = 20, async = true)
    private fun sweepPending() {
        isServerStarted = true
        if (!isEnabled.get()) return
        if (maps.isEmpty()) return
        maps.forEach { it.cleanupPending() }
    }

    /**
     * 玩家进入服务器时分配新的会话代。
     */
    @SubscribeEvent(EventPriority.LOWEST)
    private fun onJoin(event: PlayerJoinEvent) {
        if (!isEnabled.get()) return
        val uuid = event.player.uniqueId
        val epoch = epochCounter.incrementAndGet()
        onlinePlayers += uuid
        epochs[uuid] = epoch
    }

    /**
     * 玩家主动离线时清理会话。
     */
    @SubscribeEvent(EventPriority.MONITOR)
    private fun onQuit(event: PlayerQuitEvent) {
        if (!isEnabled.get()) return
        endSession(event.player)
    }

    /**
     * 结束玩家会话：移除会话代并通知所有注册容器。
     */
    private fun endSession(player: Player) {
        val uuid = player.uniqueId
        onlinePlayers -= uuid
        val epoch = epochs.remove(uuid) ?: return
        maps.forEach { it.invalidate(uuid, epoch) }
    }

    /**
     * 在第一次容器注册时，为当前在线玩家补齐会话代，保证后续查询可以立即生效。访问 Bukkit 名单时会强制切换到主线程。
     */
    private fun initializeOnlinePlayers() {
        // 仅在服务器启动后执行，避免在初始化阶段触发事件
        if (!isServerStarted) return
        runSync {
            Bukkit.getOnlinePlayers().forEach { player ->
                val uuid = player.uniqueId
                onlinePlayers += uuid
                epochs.computeIfAbsent(uuid) { epochCounter.incrementAndGet() }
            }
        }
    }
}
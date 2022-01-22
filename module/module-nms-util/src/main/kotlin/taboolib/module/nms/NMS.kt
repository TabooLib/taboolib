package taboolib.module.nms

import com.google.gson.JsonObject
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.submit
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion.major
import taboolib.module.nms.i18n.I18n
import taboolib.module.nms.type.LightType
import taboolib.module.nms.type.Toast
import taboolib.module.nms.type.ToastBackground
import taboolib.module.nms.type.ToastFrame
import taboolib.platform.BukkitPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

private val classJsonElement = Class.forName("com.google.gson.JsonElement")

private val scoreboardMap = ConcurrentHashMap<UUID, PacketScoreboard>().also {
    registerBukkitListener(PlayerQuitEvent::class.java, priority = EventPriority.NORMAL) { event ->
        it.remove(event.player.uniqueId)
    }
}

private val toastMap = ConcurrentHashMap<Toast, NamespacedKey>()

val nmsGeneric = nmsProxy<NMSGeneric>()

val nmsScoreboard = nmsProxy<NMSScoreboard>()

/**
 * 获取物品NBT数据
 */
fun ItemStack.getItemTag(): ItemTag {
    if (isAir()) {
        error("ItemStack must be not null.")
    }
    return nmsGeneric.getItemTag(this)
}

/**
 * 写入物品NBT数据
 */
fun ItemStack.setItemTag(itemTag: ItemTag): ItemStack {
    if (isAir()) {
        error("ItemStack must be not null.")
    }
    return nmsGeneric.setItemTag(this, itemTag)
}

/**
 * 获得物品的名称，如果没有则返回译名
 */
fun ItemStack.getName(player: Player? = null): String {
    return if (itemMeta?.hasDisplayName() == true) itemMeta!!.displayName else getI18nName(player)
}

/**
 * 获取物品内部名称
 */
fun ItemStack.getInternalKey(): String {
    return nmsGeneric.getKey(this)
}

/**
 * 获取物品内部名称
 */
fun ItemStack.getInternalName(): String {
    return nmsGeneric.getName(this)
}

fun ItemStack.getI18nName(player: Player? = null): String {
    return I18n.instance.getName(player, this)
}

/**
 * 获取实体内部名称
 */
fun Entity.getInternalName(): String {
    return nmsGeneric.getName(this)
}

fun Entity.getI18nName(player: Player? = null): String {
    return I18n.instance.getName(player, this)
}

/**
 * 获取附魔内部名称
 */
fun Enchantment.getInternalName(): String {
    return nmsGeneric.getEnchantmentKey(this)
}

fun Enchantment.getI18nName(player: Player? = null): String {
    return I18n.instance.getName(player, this)
}

/**
 * 获取药水效果内部名称
 */
fun PotionEffectType.getInternalName(): String {
    return nmsGeneric.getPotionEffectTypeKey(this)
}

fun PotionEffectType.getI18nName(player: Player? = null): String {
    return I18n.instance.getName(player, this)
}

/**
 * 生成实体并在生成之前执行特定行为
 */
fun <T : Entity> Location.spawnEntity(entity: Class<T>, func: Consumer<T>) {
    nmsGeneric.spawnEntity(this, entity, func)
}

/**
 * 创建光源
 *
 * @param lightLevel 光照等级
 * @param lightType 光源类型
 * @param update 是否更新区块光照
 * @param viewers 可见玩家
 */
fun Block.createLight(
    lightLevel: Int,
    lightType: LightType = LightType.ALL,
    update: Boolean = true,
    viewers: Collection<Player> = Bukkit.getOnlinePlayers(),
): Boolean {
    if (MinecraftVersion.majorLegacy < 11200) {
        error("Not supported yet.")
    }
    if (nmsGeneric.getRawLightLevel(this, lightType) > lightLevel) {
        nmsGeneric.deleteLight(this, lightType)
    }
    val result = nmsGeneric.createLight(this, lightType, lightLevel)
    if (update) {
        if (MinecraftVersion.isUniversal) {
            nmsGeneric.updateLightUniversal(this, lightType, viewers)
        } else {
            // 更新邻边区块 (为了防止光只在一个区块的尴尬局面)
            (-1..1).forEach { x ->
                (-1..1).forEach { z ->
                    nmsGeneric.updateLight(world.getChunkAt(chunk.x + x, chunk.z + z), viewers)
                }
            }
        }
    }
    return result
}

/**
 * 删除光源
 *
 * @param lightType 光源类型
 * @param update 是否更新区块光照
 * @param viewers 可见玩家
 */
fun Block.deleteLight(
    lightType: LightType = LightType.ALL,
    update: Boolean = true,
    viewers: Collection<Player> = Bukkit.getOnlinePlayers(),
): Boolean {
    if (MinecraftVersion.majorLegacy < 11200) {
        error("Not supported yet.")
    }
    val result = nmsGeneric.deleteLight(this, lightType)
    if (update) {
        if (MinecraftVersion.isUniversal) {
            nmsGeneric.updateLightUniversal(this, lightType, viewers)
        } else {
            // 更新邻边区块 (为了防止光只在一个区块的尴尬局面)
            (-1..1).forEach { x ->
                (-1..1).forEach { z ->
                    nmsGeneric.updateLight(world.getChunkAt(chunk.x + x, chunk.z + z), viewers)
                }
            }
        }
    }
    return result
}

/**
 * 发送记分板数据包
 * @param content 记分板内容（设置为空时注销记分板）
 */
fun Player.sendScoreboard(vararg content: String) {
    val scoreboardObj = scoreboardMap.getOrPut(uniqueId) {
        return@getOrPut PacketScoreboard(this)
    }
    if (content.isEmpty()) {
        scoreboardObj.sendContent(emptyList())
        return
    }
    scoreboardObj.run {
        sendTitle(content.firstOrNull().toString())
        sendContent(content.filterIndexed { index, _ -> index > 0 })
    }
}

/**
 * 发送虚拟 Toast 成就信息
 * @param icon 图标
 * @param message 信息
 * @param frame 成就框架
 * @param background 成就背景图片
 */
fun Player.sendToast(icon: Material, message: String, frame: ToastFrame = ToastFrame.TASK, background: ToastBackground = ToastBackground.ADVENTURE) {
    if (MinecraftVersion.majorLegacy < 11300) {
        error("Not supported yet.")
    }
    submit {
        val cache = Toast(icon, message, frame)
        val namespaceKey = toastMap.computeIfAbsent(cache) {
            inject(
                NamespacedKey(BukkitPlugin.getInstance(), "toast_${UUID.randomUUID()}"),
                toJsonToast(icon.invokeMethod<Any>("getKey").toString(), message, frame, background)
            )
        }
        // 注册成就
        grant(this@sendToast, namespaceKey)
        // 延迟注销，否则会出问题
        submit(delay = 20) {
            revoke(this@sendToast, namespaceKey)
        }
    }
}

private fun ItemStack?.isAir(): Boolean {
    return this == null || type == Material.AIR || type.name.endsWith("_AIR")
}

private fun grant(player: Player, key: NamespacedKey) {
    val advancement = Bukkit.getAdvancement(key)!!
    if (!player.getAdvancementProgress(advancement).isDone) {
        player.getAdvancementProgress(advancement).remainingCriteria.forEach {
            player.getAdvancementProgress(advancement).awardCriteria(it)
        }
    }
}

private fun revoke(player: Player, key: NamespacedKey) {
    val advancement = Bukkit.getAdvancement(key)
    if (advancement != null && player.getAdvancementProgress(advancement).isDone) {
        player.getAdvancementProgress(advancement).awardedCriteria.forEach {
            player.getAdvancementProgress(advancement).revokeCriteria(it)
        }
    }
}

private fun inject(key: NamespacedKey, toast: JsonObject): NamespacedKey {
    if (Bukkit.getAdvancement(key) == null) {
        val localMinecraftKey = obcClass("util.CraftNamespacedKey").invokeMethod<Any>("toMinecraft", key, isStatic = true)
        val localMinecraftServer = nmsClass("MinecraftServer").invokeMethod<Any>("getServer", isStatic = true)!!
        val localLootPredicateManager = localMinecraftServer.invokeMethod<Any>("getLootPredicateManager")
        val lootDeserializationContext = nmsClass("LootDeserializationContext")
            .getDeclaredConstructor(localMinecraftKey!!.javaClass, localLootPredicateManager!!.javaClass)
            .newInstance(localMinecraftKey, localLootPredicateManager)
        val localSerializedAdvancement = nmsClass("Advancement\$SerializedAdvancement").invokeMethod<Any>("a", toast, lootDeserializationContext, isStatic = true)
        if (localSerializedAdvancement != null) {
            if (major >= 9) {
                localMinecraftServer.invokeMethod<Any>("getAdvancementData")!!.getProperty<Any>("advancements")!!
                    .invokeMethod<Any>("a", HashMap(Collections.singletonMap(localMinecraftKey, localSerializedAdvancement)))
            } else {
                localMinecraftServer.invokeMethod<Any>("getAdvancementData")!!.getProperty<Any>("REGISTRY")!!
                    .invokeMethod<Any>("a", HashMap(Collections.singletonMap(localMinecraftKey, localSerializedAdvancement)))
            }
        }
    }
    return key
}

private fun eject(key: NamespacedKey): NamespacedKey {
    try {
        Bukkit.getUnsafe().removeAdvancement(key)
        val console = Bukkit.getServer().getProperty<Any>("console")!!
        val advancements = console.invokeMethod<Any>("getAdvancementData")!!.getProperty<MutableMap<Any, Any>>("REGISTRY/advancements")!!
        for ((k, v) in advancements) {
            if (v.getProperty<Any>("name/key") == key.key) {
                advancements.remove(k)
                break
            }
        }
    } catch (t: Throwable) {
        t.printStackTrace()
    }
    return key
}

private fun toJsonToast(icon: String, title: String, frame: ToastFrame, background: ToastBackground): JsonObject {
    val json = JsonObject()
    json.add("display", JsonObject().run {
        this.add("icon", JsonObject().run {
            this.addProperty("item", icon)
            this
        })
        this.addProperty("title", title)
        this.addProperty("description", "")
        this.addProperty("background", background.url)
        this.addProperty("frame", frame.name.lowercase(Locale.getDefault()))
        this.addProperty("announce_to_chat", false)
        this.addProperty("show_toast", true)
        this.addProperty("hidden", true)
        this
    })
    json.add("criteria", JsonObject().run {
        this.add("IMPOSSIBLE", JsonObject().run {
            this.addProperty("trigger", "minecraft:impossible")
            this
        })
        this
    })
    return json
}

/**
 * 记分板缓存
 */
private class PacketScoreboard(val player: Player) {

    private var currentTitle = ""
    private val currentContent = HashMap<Int, String>()
    var deleted = false

    init {
        nmsScoreboard.setupScoreboard(player, true)
        nmsScoreboard.display(player)
        sendTitle(currentTitle)
    }

    fun sendTitle(title: String) {
        if (currentTitle != title) {
            currentTitle = title
            nmsScoreboard.setDisplayName(player, title)
        }
    }

    fun sendContent(lines: List<String>) {
        if (deleted) {
            nmsScoreboard.setupScoreboard(player, false, currentTitle)
            nmsScoreboard.display(player)
        }
        deleted = nmsScoreboard.changeContent(player, lines, currentContent)
        currentContent.clear()
        currentContent.putAll(lines.mapIndexed { index, s -> index to s }.toMap())
    }
}
@file:Suppress("DEPRECATION")
@file:Isolated

package taboolib.module.nms

import com.google.gson.JsonObject
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.Isolated
import taboolib.common.UnsupportedVersionException
import taboolib.common.platform.function.submit
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.MinecraftVersion.V1_17
import taboolib.module.nms.MinecraftVersion.V1_18
import taboolib.module.nms.type.Toast
import taboolib.module.nms.type.ToastBackground
import taboolib.module.nms.type.ToastFrame
import taboolib.platform.BukkitPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val toastMap = ConcurrentHashMap<Toast, NamespacedKey>()

private val classJsonElement by unsafeLazy { Class.forName("com.google.gson.JsonElement") }

private val classCraftNamespacedKey by unsafeLazy { obcClass("util.CraftNamespacedKey") }

private val classMinecraftServer by unsafeLazy { nmsClass("MinecraftServer") }

private val classSerializedAdvancement by unsafeLazy { nmsClass("Advancement\$SerializedAdvancement") }

private val constructorLootDeserializationContext by unsafeLazy { nmsClass("LootDeserializationContext").declaredConstructors[0] }

private val lootDataManager by unsafeLazy {
    when {
        // 1.20
        MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_20) -> minecraftServerObject.invokeMethod<Any>("getLootData")
        // 1.17
        MinecraftVersion.isHigherOrEqual(V1_17) -> minecraftServerObject.invokeMethod<Any>("getPredicateManager")
        // 其他版本
        else -> minecraftServerObject.invokeMethod<Any>("getLootPredicateManager")
    }
}

private val advancements by unsafeLazy {
    when {
        // 1.18
        MinecraftVersion.isHigherOrEqual(V1_18) -> minecraftServerObject.invokeMethod<Any>("getAdvancements")!!.getProperty<Any>("advancements")!!
        // 1.17
        MinecraftVersion.isHigherOrEqual(V1_17) -> minecraftServerObject.invokeMethod<Any>("getAdvancementData")!!.getProperty<Any>("advancements")!!
        // 其他版本
        else -> minecraftServerObject.invokeMethod<Any>("getAdvancementData")!!.getProperty<Any>("REGISTRY")!!
    }
}

private val advancementsMap by unsafeLazy { advancements.getProperty<MutableMap<Any, Any>>("advancements")!! }

/**
 * 发送虚拟 Toast 成就信息
 * @param icon 图标
 * @param message 信息
 * @param frame 成就框架
 * @param background 成就背景图片
 */
fun Player.sendToast(icon: Material, message: String, frame: ToastFrame = ToastFrame.TASK, background: ToastBackground = ToastBackground.ADVENTURE) {
    if (MinecraftVersion.isLower(MinecraftVersion.V1_13)) {
        throw UnsupportedVersionException()
    }
    val cache = Toast(icon, message, frame)
    val jsonToast = toJsonToast(icon.invokeMethod<Any>("getKey").toString(), message, frame, background)
    // 在主线程操作
    submit {
        val namespaceKey = toastMap.getOrPut(cache) { injectAdvancement(NamespacedKey(BukkitPlugin.getInstance(), "toast_${UUID.randomUUID()}"), jsonToast) }
        // 注册成就
        grant(this@sendToast, namespaceKey)
        // 延迟注销，否则会出问题
        submit(delay = 20) { revoke(this@sendToast, namespaceKey) }
    }
}

/**
 * 赋予玩家成就
 */
private fun grant(player: Player, key: NamespacedKey) {
    val advancement = Bukkit.getAdvancement(key)!!
    if (!player.getAdvancementProgress(advancement).isDone) {
        player.getAdvancementProgress(advancement).remainingCriteria.forEach {
            player.getAdvancementProgress(advancement).awardCriteria(it)
        }
    }
}

/**
 * 注销玩家成就
 */
private fun revoke(player: Player, key: NamespacedKey) {
    val advancement = Bukkit.getAdvancement(key)
    if (advancement != null && player.getAdvancementProgress(advancement).isDone) {
        player.getAdvancementProgress(advancement).awardedCriteria.forEach {
            player.getAdvancementProgress(advancement).revokeCriteria(it)
        }
    }
}

/**
 * 将成就注入到服务器
 */
private fun injectAdvancement(key: NamespacedKey, toast: JsonObject): NamespacedKey {
    if (Bukkit.getAdvancement(key) == null) {
        // 获取 MinecraftKey
        val localMinecraftKey = classCraftNamespacedKey.invokeMethod<Any>("toMinecraft", key, isStatic = true)
        // 创建 LootDeserializationContext
        val lootDeserializationContext = constructorLootDeserializationContext.newInstance(localMinecraftKey, lootDataManager)
        // 创建 SerializedAdvancement (public static SerializedAdvancement a(JsonObject var0, LootDeserializationContext var1))
        val localSerializedAdvancement = classSerializedAdvancement.invokeMethod<Any>("a", toast, lootDeserializationContext, isStatic = true)
        if (localSerializedAdvancement != null) {
            // 注入到服务器
            advancements.invokeMethod<Any>("a", hashMapOf(localMinecraftKey to localSerializedAdvancement))
        }
    }
    return key
}

/**
 * 注销成就
 */
private fun ejectAdvancement(key: NamespacedKey): NamespacedKey {
    try {
        // 移除成就
        Bukkit.getUnsafe().removeAdvancement(key)
        // 从 AdvancementsMap 中移除
        for ((k, v) in advancementsMap) {
            if (v.getProperty<Any>("name/key") == key.key) {
                advancementsMap.remove(k)
                break
            }
        }
    } catch (t: Throwable) {
        t.printStackTrace()
    }
    return key
}

/**
 * 生成成就 Json
 */
private fun toJsonToast(icon: String, title: String, frame: ToastFrame, background: ToastBackground, customModelData: Int = 0): JsonObject {
    val json = JsonObject()
    json.add("display", JsonObject().run {
        this.add("icon", JsonObject().run {
            this.addProperty("item", icon)
            this.addProperty("nbt", "{CustomModelData:$customModelData}")
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
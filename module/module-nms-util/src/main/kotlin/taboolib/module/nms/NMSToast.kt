package taboolib.module.nms

import com.google.gson.JsonObject
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.platform.function.submit
import taboolib.module.nms.MinecraftVersion.major
import taboolib.module.nms.type.Toast
import taboolib.module.nms.type.ToastBackground
import taboolib.module.nms.type.ToastFrame
import taboolib.platform.BukkitPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val classJsonElement = Class.forName("com.google.gson.JsonElement")

private val toastMap = ConcurrentHashMap<Toast, NamespacedKey>()

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
        submit(delay = 20) { revoke(this@sendToast, namespaceKey) }
    }
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
        val localLootPredicateManager = if (major >= 9) {
            localMinecraftServer.invokeMethod<Any>("getPredicateManager")
        } else {
            localMinecraftServer.invokeMethod<Any>("getLootPredicateManager")
        }
        val lootDeserializationContext = nmsClass("LootDeserializationContext")
            .getDeclaredConstructor(localMinecraftKey!!.javaClass, localLootPredicateManager!!.javaClass)
            .newInstance(localMinecraftKey, localLootPredicateManager)
        val localSerializedAdvancement =
            nmsClass("Advancement\$SerializedAdvancement").invokeMethod<Any>("a", toast, lootDeserializationContext, isStatic = true)
        if (localSerializedAdvancement != null) {
            if (major >= 10) {
                localMinecraftServer.invokeMethod<Any>("getAdvancements")!!.getProperty<Any>("advancements")!!
                    .invokeMethod<Any>("a", HashMap(Collections.singletonMap(localMinecraftKey, localSerializedAdvancement)))
            } else if (major >= 9) {
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
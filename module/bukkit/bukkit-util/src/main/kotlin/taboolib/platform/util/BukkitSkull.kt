@file:Suppress("DEPRECATION", "HttpUrlsUsage", "MemberVisibilityCanBePrivate")

package taboolib.platform.util

import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.common5.util.decodeBase64
import taboolib.library.xseries.XMaterial
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.function.Function

/**
 * ExamplePlugin
 * taboolib.platform.util.Heads
 *
 * @author mical
 * @since 2024/9/17 10:29
 */
object BukkitSkull {

    /** 是否使用 1.12.1 及以上版本 (setOwningPlayer) */
    private val use12 = runCatching { SkullMeta::class.java.getDeclaredMethod("setOwningPlayer") }.isSuccess
    /** 是否使用 1.13 及以上版本 (扁平化后) */
    private val use13 = runCatching { Material.PLAYER_HEAD }.isSuccess
    /** 是否使用 1.18.2 及以上版本, 该版本拥有 org.bukkit.profile.PlayerProfile */
    private val use18 = runCatching { Class.forName("org.bukkit.profile.PlayerProfile") }.isSuccess

    /** 旧版本 Gson 的 JsonParser 没有 parseString 静态方法, 要使用这个 */
    private val JSON_PARSER = JsonParser()

    /** 默认头颅 */
    private val DEFAULT_HEAD = XMaterial.PLAYER_HEAD.parseItem()!!.apply {
        // 扁平化前要确保头颅类型为玩家头
        // 扁平化前「玩家头」的子 ID 为 3
        if (!use13) {
            durability = 3
        }
    }

    /** 获取 Property 值的方法，兼容高低不同版本 */
    private val getProfileMethod = try {
        Property::class.java.getDeclaredMethod("value")
    } catch (_: Throwable) {
        Property::class.java.getDeclaredMethod("getValue")
    }
    /** record ResolvableProfile(GameProfile gameProfile) */
    private val gameProfileMethod = runCatching {
        Class.forName("net.minecraft.world.item.component.ResolvableProfile")
            .declaredFields
            .firstOrNull { it.type == GameProfile::class.java }
            ?.apply { isAccessible = true }
    }.getOrNull()

    /**
     * 应用头颅纹理到物品上 (无自定义处理函数版本)
     * @param item 要应用纹理的物品, 会对源物品进行修改, 如果为 null 或 AIR 则由我们创建一个物品
     * @param headBase64 头颅纹理的 Base64 编码或玩家名称
     * @return 应用了纹理的物品
     */
    fun applySkull(item: ItemStack, headBase64: String): ItemStack {
        return applySkull(item.apply {
            // 扁平化前要确保头颅类型为玩家头
            // 扁平化前「玩家头」的子 ID 为 3
            if (!use13) {
                durability = 3
            }
        }, headBase64, null)
    }

    /**
     * 不传入头颅源物品, 通过给定头颅纹理 Base64 或玩家名称来构建一个新头
     * @param headBase64 头颅纹理的 Base64 编码或玩家名称
     * @param func 自定义处理函数, 若为 null 或返回一个为 null 的 ItemStack 则尝试通过原版方法应用玩家纹理
     * @return 应用了纹理的物品
     */
    fun applySkull(headBase64: String, func: Function<ItemStack, ItemStack?>?): ItemStack {
        return applySkull(DEFAULT_HEAD.clone(), headBase64, func)
    }

    /**
     * 不传入头颅源物品, 通过给定头颅纹理 Base64 或玩家名称来构建一个新头
     * @param headBase64 头颅纹理的 Base64 编码或玩家名称
     * @return 应用了纹理的物品
     */
    fun applySkull(headBase64: String): ItemStack {
        return applySkull(DEFAULT_HEAD.clone(), headBase64, null)
    }

    /**
     * 应用头颅纹理到物品上
     * 当输入为玩家名称时, 可以传入来调用第三方插件处理头, 例如 SkinsRestorer, 适用于离线服务器等情况
     * @param item 要应用纹理的物品, 会对源物品进行修改, 如果为 null 或 AIR 则由我们创建一个物品
     * @param headBase64 头颅纹理的 Base64 编码或玩家名称
     * @param func 自定义处理函数, 若为 null 或返回一个为 null 的 ItemStack 则尝试通过原版方法应用玩家纹理
     * @return 应用了纹理的物品
     */
    fun applySkull(item: ItemStack, headBase64: String, func: Function<ItemStack, ItemStack?>?): ItemStack {
        val meta = item.itemMeta as? SkullMeta ?: return item
        // 判定传入为玩家名
        if (headBase64.length <= 20) {
            // 如果应用了处理函数, 则尝试通过自定义函数处理物品
            if (func != null) {
                val result = func.apply(item)
                // 尝试返回自定义函数中返回的物品
                if (result != null) {
                    return result
                }
            }
            // 如果没有自定义处理函数, 或自定义函数处理结果为空 (一般是开发者认为现有 API 因为种种原因无法处理)
            if (use12) {
                meta.owningPlayer = Bukkit.getOfflinePlayer(headBase64)
            } else {
                meta.owner = headBase64
            }
            item.itemMeta = meta
            return item
        }
        // 下面这是 Spigot 1.18.1 发布之后添加的头颅工具, 准确来说从 1.18.2 开始
        if (use18) {
            val profile = Bukkit.createPlayerProfile(UUID(0, 0), "TabooLib")
            val textures = profile.textures
            // NOTICE 下面这一行代码我不太清楚是如何工作的, 但是它工作正常. 来自 TrMenu
            val texture = if (headBase64.length in 60..100) encodeTexture(headBase64) else headBase64
            val url = URL(getTextureURLFromBase64(texture))
            try {
                textures.skin = url
            } catch (e: MalformedURLException) {
                throw IllegalStateException("Invalid skull base64 content", e)
            }
            meta.ownerProfile = profile
        } else {
            // 如果使用 1.18.1 及以下版本, 则使用老方法处理
            val profile = GameProfile(UUID(0, 0), "TabooLib")
            val texture = if (headBase64.length in 60..100) encodeTexture(headBase64) else headBase64
            profile.properties.put("textures", Property("textures", texture, "TabooLib_TexturedSkull"))

            meta.setProperty("profile", profile)
        }
        item.itemMeta = meta
        return item
    }

    /**
     * 获取头颅的纹理字符串, 也就是 Base64 文本
     * @param meta 头颅物品的元数据
     * @return 纹理字符串，如果没有则返回空字符串
     */
    fun getSkullValue(meta: SkullMeta): String {
        var profile: Any? = meta.getProperty("profile") ?: return ""
        // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/inventory/CraftMetaSkull.java?at=8d52226914ce4b99f35be3d6954f35616d92476d
        // SPIGOT-7882, #1467: Fix conversion of name in Profile Component to empty if it is missing
        // 省流版: 1.21.1 的新版本中 Spigot 将 CraftMetaSkull 中 profile 的类型由 com.mojang.authlib.GameProfile 修改为 net.minecraft.world.item.component.ResolvableProfile
        // 但其中有封装 GameProfile, 可以直接调用
        if (profile !is GameProfile) {
            profile = gameProfileMethod?.get(profile)
        }
        if (profile is GameProfile) {
            val properties = profile.properties["textures"] ?: return ""
            if (properties.isEmpty()) return ""

            for (property in properties) {
                val value: String = try {
                    getProfileMethod.invoke(property) as String
                } catch (_: Throwable) {
                    continue
                }
                if (value.isNotEmpty()) return value
            }
            return ""
        }
        return ""
    }

    /**
     * 将纹理 ID 编码为完整的纹理 Base64 字符串
     * @param input 纹理 ID
     * @return 编码后的 Base64 字符串
     */
    private fun encodeTexture(input: String): String {
        return with(Base64.getEncoder()) {
            encodeToString("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/$input\"}}}".toByteArray())
        }
    }

    /**
     * 从 Base64 编码的纹理数据中提取纹理 URL
     * @param headBase64 Base64 编码的纹理数据
     * @return 纹理 URL
     */
    private fun getTextureURLFromBase64(headBase64: String): String {
        return JSON_PARSER
            .parse(String(headBase64.decodeBase64()))
            .asJsonObject
            .getAsJsonObject("textures")
            .getAsJsonObject("SKIN")
            .get("url")
            .asString
    }
}
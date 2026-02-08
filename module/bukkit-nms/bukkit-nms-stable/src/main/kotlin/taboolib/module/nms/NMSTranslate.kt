package taboolib.module.nms

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.material.MaterialData
import org.bukkit.potion.PotionEffectType
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.UnsupportedVersionException
import taboolib.common.util.unsafeLazy
import taboolib.library.xseries.XMaterial
import taboolib.module.nms.MinecraftLanguage.LanguageKey.Type
import taboolib.module.nms.legacy.NMSPotionEffect
import taboolib.platform.util.buildItem
import java.lang.reflect.Method

/**
 * 缓存材质,实体,附魔,药水效果原版名
 */
private val translate: HashMap<String, String> = HashMap()

/**
 * 获取物品的名称（若存在 displayName 则返回 displayName，反之获取译名）
 */
fun ItemStack.getName(player: Player? = null): String {
    return if (itemMeta?.hasDisplayName() == true) itemMeta!!.displayName else getI18nName(player)
}

/**
 * 获取 Material 的译名
 * 1.12-版本不建议使用
 */
fun Material.getI18nName(player: Player? = null): String {
    val name = if (MinecraftVersion.isLowerOrEqual(MinecraftVersion.V1_12)) name + "_${MaterialData(this).data}" else name
    return translate[name] ?: buildItem(this).getI18nName(player)
}

/**
 * 获取 XMaterial 的译名
 */
fun XMaterial.getI18nName(player: Player? = null): String {
    return translate[name] ?: parseItem()?.getI18nName(player) ?: "NO_ITEM"
}

/**
 * 获取物品的译名
 */
fun ItemStack.getI18nName(player: Player? = null): String {
    val file = player?.getMinecraftLanguageFile() ?: MinecraftLanguage.getDefaultLanguageFile() ?: return "NO_LOCALE"
    val name = if (MinecraftVersion.isLowerOrEqual(MinecraftVersion.V1_12)) type.name + "_${data?.data ?: 0}" else type.name
    return translate[name] ?: let {
        val value = file[getLanguageKey()] ?: getLanguageKey().path
        translate[name] = value
        value
    }
}

/**
 * 获取实体修改后的名字,如果没有被修改则是原版名
 */
fun Entity.getDisplayName(player: Player? = null): String {
    return customName ?: getI18nName(player)
}

/**
 * 获取实体的译名
 */
fun Entity.getI18nName(player: Player? = null): String {
    return translate[name] ?: (player?.getMinecraftLanguageFile() ?: MinecraftLanguage.getDefaultLanguageFile())?.let {
        val value = it[getLanguageKey()] ?: getLanguageKey().path
        translate[name] = value
        value
    } ?: "NO_LOCALE"
}

/**
 * 获取附魔的译名
 */
fun Enchantment.getI18nName(player: Player? = null): String {
    return translate[name] ?: (player?.getMinecraftLanguageFile() ?: MinecraftLanguage.getDefaultLanguageFile())?.let {
        val value = it[getLanguageKey()] ?: getLanguageKey().path
        translate[name] = value
        value
    } ?: "NO_LOCALE"
}

/**
 * 获取药水效果的译名
 */
fun PotionEffectType.getI18nName(player: Player? = null): String {
    return translate[name] ?: (player?.getMinecraftLanguageFile() ?: MinecraftLanguage.getDefaultLanguageFile())?.let {
        val value = it[getLanguageKey()] ?: getLanguageKey().path
        translate[name] = value
        value
    } ?: "NO_LOCALE"
}

/**
 * 获取物品的 Key，例如 `diamond_sword`
 */
fun ItemStack.getKey(): String {
    return NMSTranslate.instance.getKey(this)
}

/**
 * 获取物品的语言文件节点，例如 `item.minecraft.diamond_sword`
 */
fun ItemStack.getLanguageKey(): MinecraftLanguage.LanguageKey {
    return NMSTranslate.instance.getLanguageKey(this)
}

/**
 * 获取附魔的语言文件节点，例如 `enchantment.minecraft.sharpness`
 */
fun Enchantment.getLanguageKey(): MinecraftLanguage.LanguageKey {
    return NMSTranslate.instance.getLanguageKey(this)
}

/**
 * 获取药水效果的语言文件节点，例如 `effect.minecraft.regeneration`
 */
fun PotionEffectType?.getLanguageKey(): MinecraftLanguage.LanguageKey {
    return NMSTranslate.instance.getLanguageKey(this)
}

/**
 * TabooLib
 * taboolib.module.nms.NMSTranslate
 *
 * @author 坏黑
 * @since 2023/8/5 03:48
 */
abstract class NMSTranslate {

    /** 获取物品的 Key，例如 `diamond_sword` */
    abstract fun getKey(itemStack: ItemStack): String

    /** 获取物品的语言文件节点，例如 `item.minecraft.diamond_sword` */
    abstract fun getLanguageKey(itemStack: ItemStack): MinecraftLanguage.LanguageKey

    /** 获取附魔的语言文件节点，例如 `enchantment.minecraft.sharpness` */
    abstract fun getLanguageKey(enchantment: Enchantment): MinecraftLanguage.LanguageKey

    /** 获取药水效果的语言文件节点，例如 `effect.minecraft.regeneration` */
    abstract fun getLanguageKey(potionEffectType: PotionEffectType?): MinecraftLanguage.LanguageKey

    companion object {

        val instance by unsafeLazy { nmsProxy<NMSTranslate>() }
    }
}

// region NMSTranslateImpl
class NMSTranslateImpl : NMSTranslate() {

    /**
     * 是否支持 Translatable
     */
    val isTranslatableSupported by lazy {
        if (MinecraftVersion.versionId >= 11903) {
            runCatching { Class.forName("org.bukkit.Translatable") }.isSuccess
        } else false
    }

    // region Reflection Cache

    /**
     * 用于获取物品的语言文件名称的方法
     * 限定名称; 参数只有一个; 参数类型是 [net.minecraft.server] 包下的 ItemStack; 返回值是 String
     */
    val itemLocaleNameMethod: Method? = net.minecraft.server.v1_12_R1.Item::class.java.declaredMethods.find {
        checkName0(it.name) && it.parameterTypes.size == 1 && it.parameterTypes[0] == net.minecraft.server.v1_12_R1.ItemStack::class.java && it.returnType == String::class.java
    }

    /**
     * 用于获取物品的语言文件节点的方法
     * 限定名称; 参数只有一个; 参数类型是 [net.minecraft.server] 包下的 ItemStack; 返回值是 String
     */
    val itemLocaleKeyMethod: Method? = net.minecraft.server.v1_12_R1.Item::class.java.declaredMethods.find {
        checkName1(it.name) && it.parameterTypes.size == 1 && it.parameterTypes[0] == net.minecraft.server.v1_12_R1.ItemStack::class.java && it.returnType == String::class.java
    }

    /**
     * 1.19.3, 1.20 -> BuiltInRegistries.MOB_EFFECT
     */
    val mobEffectBuiltInRegistries by unsafeLazy { nmsClass("BuiltInRegistries").getProperty<Any>("MOB_EFFECT", isStatic = true)!! }

    /**
     * 1.17, 1.19.2 -> IRegistry.MOB_EFFECT
     */
    val mobEffectIRegistry by unsafeLazy { nmsClass("IRegistry").getProperty<Any>("MOB_EFFECT", isStatic = true)!! }

    init {
        itemLocaleNameMethod?.isAccessible = true
        itemLocaleKeyMethod?.isAccessible = true
    }
    // endregion

    fun getNMSCopy(itemStack: ItemStack): Any {
        return org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asNMSCopy(itemStack)
    }

    fun getBukkitCopy(itemStack: Any): ItemStack {
        return org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asBukkitCopy(itemStack as net.minecraft.server.v1_12_R1.ItemStack)
    }

    override fun getKey(itemStack: ItemStack): String {
        return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
            itemStack.type.key.key
        } else {
            val nmsItemStack = getNMSCopy(itemStack) as net.minecraft.server.v1_8_R3.ItemStack
            val nmsItem = nmsItemStack.item
            val name = nmsItem.getProperty<String>("name")!!
            name.toCharArray().joinToString("") { if (it.isUpperCase()) "_${it.lowercase()}" else it.toString() }
        }
    }

    /**
     * 在 1.12 及以下版本，包含名称变种的物品会被特殊处理，具体表现为：
     *
     * **ItemPotion**
     * ```
     * public String a(ItemStack var1) {
     *     if (var1.getData() == 0) {
     *         return LocaleI18n.get("item.emptyPotion.name").trim();
     *     } else {
     *         String var2 = "";
     *         if (f(var1.getData())) {
     *             var2 = LocaleI18n.get("potion.prefix.grenade").trim() + " ";
     *         }
     *         List var3 = Items.POTION.h(var1);
     *         String var4;
     *         if (var3 != null && !var3.isEmpty()) {
     *             var4 = ((MobEffect)var3.get(0)).g();
     *             var4 = var4 + ".postfix";
     *             return var2 + LocaleI18n.get(var4).trim();
     *         } else {
     *             var4 = PotionBrewer.c(var1.getData());
     *             return LocaleI18n.get(var4).trim() + " " + super.a(var1);
     *         }
     *     }
     * }
     * ```
     * **ItemMonsterEgg**
     * ```
     * public String a(ItemStack itemstack) {
     *     String s = LocaleI18n.get(this.getName() + ".name").trim();
     *     String s1 = EntityTypes.b(itemstack.getData());
     *     if (s1 != null) {
     *         s = s + " " + LocaleI18n.get("entity." + s1 + ".name");
     *     }
     *     return s;
     * }
     * ```
     * 通过上述代码可以看出，服务端本身就无法直接获取关于 [Potion] 和 [MonsterEgg] 的准确语言文件节点，需要依赖拼接完成。
     *
     * 这种逆天的语言文件拼接在 1.13 版本后被移除。
     */
    override fun getLanguageKey(itemStack: ItemStack): MinecraftLanguage.LanguageKey {
        // 使用 Translatable 接口
        if (isTranslatableSupported) {
            return MinecraftLanguage.LanguageKey(Type.NORMAL, itemStack.translationKey)
        }
        // region Legacy Version
        // 1.11 以下版本没有针对空物品的译名，因此直接返回 "air"
        if (MinecraftVersion.isLower(MinecraftVersion.V1_11) && itemStack.type == Material.AIR) {
            return MinecraftLanguage.LanguageKey(Type.DEFAULT, "air")
        }
        val nmsItemStack = getNMSCopy(itemStack) as net.minecraft.server.v1_12_R1.ItemStack
        val nmsItem = nmsItemStack.item
        return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
            itemLocaleKeyMethod ?: throw UnsupportedVersionException()
            MinecraftLanguage.LanguageKey(Type.NORMAL, itemLocaleKeyMethod.invoke(nmsItem, nmsItemStack).toString())
        } else {
            // 对 ItemMonsterEgg 进行特殊处理
            if (nmsItem is net.minecraft.server.v1_12_R1.ItemMonsterEgg) {
                // 获取实体类型
                val entityKey = when (MinecraftVersion.major) {
                    // 1.8 通过附加值判定实体类型
                    MinecraftVersion.V1_8 -> net.minecraft.server.v1_8_R3.EntityTypes.b(nmsItemStack.data)
                    // 1.9, 1.10
                    // ItemMonsterEgg.h 返回值为 String
                    MinecraftVersion.V1_9, MinecraftVersion.V1_10 -> {
                        @Suppress("CAST_NEVER_SUCCEEDS")
                        net.minecraft.server.v1_9_R2.EntityTypes.a(net.minecraft.server.v1_9_R2.ItemMonsterEgg.h(nmsItemStack as net.minecraft.server.v1_9_R2.ItemStack))
                    }
                    // 1.12
                    // ItemMonsterEgg.h 返回值为 MinecraftKey
                    else -> net.minecraft.server.v1_12_R1.EntityTypes.a(net.minecraft.server.v1_12_R1.ItemMonsterEgg.h(nmsItemStack))
                }
                MinecraftLanguage.LanguageKey(Type.SPECIAL, "${nmsItem.name}.name", if (entityKey != null) "entity.$entityKey.name" else null)
            } else {
                itemLocaleNameMethod ?: throw UnsupportedVersionException()
                // 获取译名
                val localeName = itemLocaleNameMethod.invoke(nmsItem, nmsItemStack).toString()
                val localeLanguage = net.minecraft.server.v1_8_R3.LocaleI18n::class.java.getProperty<net.minecraft.server.v1_8_R3.LocaleLanguage>("a", true, remap = false)!!
                val localeMap = localeLanguage.getProperty<Map<String, String>>("d", remap = false)!!
                // 逆向查找语言文件节点
                val localeKey = localeMap.entries.firstOrNull { it.value == localeName }?.key
                if (localeKey == null) {
                    // 对于一些特殊的物品，例如：修改 SkullOwner 后的头、成书等，译名会被修改，导致无法获取到语言文件节点。
                    itemLocaleKeyMethod ?: error("Unsupported item: ${itemStack.type}.")
                    var name = itemLocaleKeyMethod.invoke(nmsItem, nmsItemStack)?.toString() ?: error("Unsupported item ${itemStack.type}")
                    // 如果物品不以 .name 结尾，则添加 .name 后缀
                    if (!name.endsWith(".name")) {
                        name += ".name"
                    }
                    MinecraftLanguage.LanguageKey(Type.SPECIAL, name)
                } else {
                    MinecraftLanguage.LanguageKey(Type.NORMAL, localeKey)
                }
            }
        }
        // endregion
    }

    /**
     * **Enchantment**, **1.12-** 表现为：
     * ```
     * public String a() {
     *     return "enchantment." + this.d;
     * }
     * ```
     * **Enchantment**, **1.13+** 表现为：
     * ```
     * protected String f() {
     *     if (this.descriptionId == null) {
     *         this.descriptionId = // 省略 ...
     *     }
     *     return this.descriptionId;
     * }
     *
     * public String g() {
     *     return this.f();
     * }
     * ```
     * **Enchantment**, **1.18+** 表现为：
     * ```
     * protected String getOrCreateDescriptionId() {
     *     // 同 1.13+
     * }
     *
     * public String getDescriptionId() {
     *     return this.getOrCreateDescriptionId();
     * }
     * ```
     */
    override fun getLanguageKey(enchantment: Enchantment): MinecraftLanguage.LanguageKey {
        // 1.12 及以下版本
        if (MinecraftVersion.isLowerOrEqual(MinecraftVersion.V1_12)) {
            return MinecraftLanguage.LanguageKey(Type.NORMAL, Craft12Enchantment.getRaw(enchantment).a())
        }
        return try {
            MinecraftLanguage.LanguageKey(Type.NORMAL, enchantment.translationKey)
        } catch (_: NoSuchMethodError) {
            MinecraftLanguage.LanguageKey(Type.NORMAL, Craft16Enchantment.getRaw(enchantment).g())
        }
    }

    /**
     * 表现形式与 [Enchantment] 接近，仅转换为 NMS 类型的方法不同。
     */
    @Suppress("UNCHECKED_CAST")
    override fun getLanguageKey(potionEffectType: PotionEffectType?): MinecraftLanguage.LanguageKey {
        if (potionEffectType == null) {
            return MinecraftLanguage.LanguageKey(Type.DEFAULT, "null")
        }
        return if (MinecraftVersion.isUniversal) {
            val descriptionId = when {
                // 使用 Translatable 接口
                isTranslatableSupported -> potionEffectType.translationKey
                // 1.17
                // 继续使用 fromId
                MinecraftVersion.isEqual(MinecraftVersion.V1_17) -> {
                    val registry = mobEffectIRegistry as net.minecraft.server.v1_16_R1.Registry<Any>
                    registry.fromId(potionEffectType.id)!!.invokeMethod<String>("c", remap = false)
                }
                // 1.18 ... 1.20
                // fromId -> byId
                else -> {
                    val registry = runCatching { mobEffectBuiltInRegistries }.getOrElse { mobEffectIRegistry }
                    registry as net.minecraft.core.Registry<Any>
                    registry.byId(potionEffectType.id)!!.invokeMethod<String>("getDescriptionId")
                }
            }
            MinecraftLanguage.LanguageKey(Type.NORMAL, descriptionId!!)
        } else {
            NMSPotionEffect.instance.getLanguageKey(potionEffectType)
        }
    }

    /** 获取物品「译名」的方法名称 */
    private fun checkName0(name: String): Boolean {
        return when (MinecraftVersion.major) {
            MinecraftVersion.V1_11, MinecraftVersion.V1_12 -> name == "b"
            else -> name == "a"
        }
    }

    /** 获取物品「语言文件节点」的方法名称 */
    private fun checkName1(name: String): Boolean {
        return when (MinecraftVersion.major) {
            MinecraftVersion.V1_8 -> name == "e_"
            MinecraftVersion.V1_9, MinecraftVersion.V1_10 -> name == "f_"
            MinecraftVersion.V1_11, MinecraftVersion.V1_12 -> name == "a"
            else -> name != "a"
        }
    }
}
// endregion NMSItemImpl

// region Typealias
private typealias Craft16Enchantment = org.bukkit.craftbukkit.v1_16_R1.enchantments.CraftEnchantment
private typealias Craft12Enchantment = org.bukkit.craftbukkit.v1_12_R1.enchantments.CraftEnchantment
// endregion

package taboolib.module.nms

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.UnsupportedVersionException
import taboolib.common.util.unsafeLazy
import java.lang.reflect.Method

/**
 * 获取物品的 Key，例如 `diamond_sword`
 */
fun ItemStack.getKey(): String {
    return nmsProxy<NMSItem>().getKey(this)
}

/**
 * 获取物品的语言文件节点，例如 `item.minecraft.diamond_sword`
 */
fun ItemStack.getLocaleKey(): LocaleKey {
    return nmsProxy<NMSItem>().getLocaleKey(this)
}

/**
 * 获取附魔的语言文件节点，例如 `enchantment.minecraft.sharpness`
 */
fun Enchantment.getLocaleKey(): LocaleKey {
    return nmsProxy<NMSItem>().getLocaleKey(this)
}

/**
 * 获取药水效果的语言文件节点，例如 `effect.minecraft.regeneration`
 */
fun PotionEffectType?.getLocaleKey(): LocaleKey {
    return nmsProxy<NMSItem>().getLocaleKey(this)
}

/**
 * TabooLib
 * taboolib.module.nms.NMSItem
 *
 * @author 坏黑
 * @since 2023/8/5 03:48
 */
abstract class NMSItem {

    /** 将 [ItemStack] 转换为 [net.minecraft.server] 下的 ItemStack */
    abstract fun getNMSCopy(itemStack: ItemStack): Any

    /** 将 [net.minecraft.server] 下的 ItemStack 转换为 [ItemStack] */
    abstract fun getBukkitCopy(itemStack: Any): ItemStack

    /** 获取物品的 Key，例如 `diamond_sword` */
    abstract fun getKey(itemStack: ItemStack): String

    /** 获取物品的语言文件节点，例如 `item.minecraft.diamond_sword` */
    abstract fun getLocaleKey(itemStack: ItemStack): LocaleKey

    /** 获取附魔的语言文件节点，例如 `enchantment.minecraft.sharpness` */
    abstract fun getLocaleKey(enchantment: Enchantment): LocaleKey

    /** 获取药水效果的语言文件节点，例如 `effect.minecraft.regeneration` */
    abstract fun getLocaleKey(potionEffectType: PotionEffectType?): LocaleKey

    companion object {

        /**
         * 获取 [ItemStack] 的 NMS 副本
         */
        fun asNMSCopy(item: ItemStack): Any {
            return nmsProxy<NMSItem>().getNMSCopy(item)
        }

        /**
         * 获取 NMS 物品的 Bukkit 副本
         */
        fun asBukkitCopy(item: Any): ItemStack {
            return nmsProxy<NMSItem>().getBukkitCopy(item)
        }
    }
}

/**
 * [NMSItem] 的实现类
 */
class NMSItemImpl : NMSItem() {

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

    override fun getNMSCopy(itemStack: ItemStack): Any {
        return org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asNMSCopy(itemStack)
    }

    override fun getBukkitCopy(itemStack: Any): ItemStack {
        return org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asBukkitCopy(itemStack as net.minecraft.server.v1_12_R1.ItemStack)
    }

    override fun getKey(itemStack: ItemStack): String {
        return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
            itemStack.type.key.key
        } else {
            val nmsItemStack = getNMSCopy(itemStack) as net.minecraft.server.v1_8_R3.ItemStack
            val nmsItem = nmsItemStack.item
            val name = nmsItem.getProperty<String>("name")!!
            name.toCharArray().joinToString { if (it.isUpperCase()) "_${it.lowercase()}" else it.toString() }
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
    override fun getLocaleKey(itemStack: ItemStack): LocaleKey {
        // 1.11 以下版本没有针对空物品的译名，因此直接返回 "air"
        if (MinecraftVersion.isLower(MinecraftVersion.V1_11) && itemStack.type == Material.AIR) {
            return LocaleKey("D", "air")
        }
        val nmsItemStack = getNMSCopy(itemStack) as net.minecraft.server.v1_12_R1.ItemStack
        val nmsItem = nmsItemStack.item
        return if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_13)) {
            itemLocaleKeyMethod ?: throw UnsupportedVersionException()
            LocaleKey("N", itemLocaleKeyMethod.invoke(nmsItem, nmsItemStack).toString())
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
                LocaleKey("S", "${nmsItem.name}.name", if (entityKey != null) "entity.$entityKey.name" else null)
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
                    LocaleKey("S", name)
                } else {
                    LocaleKey("N", localeKey)
                }
            }
        }
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
    override fun getLocaleKey(enchantment: Enchantment): LocaleKey {
        if (MinecraftVersion.isLowerOrEqual(MinecraftVersion.V1_12)) {
            return LocaleKey("N", org.bukkit.craftbukkit.v1_12_R1.enchantments.CraftEnchantment.getRaw(enchantment).a())
        }
        return try {
            LocaleKey("N", org.bukkit.craftbukkit.v1_20_R1.enchantments.CraftEnchantment.getRaw(enchantment).descriptionId)
        } catch (_: NoSuchMethodError) {
            LocaleKey("N", org.bukkit.craftbukkit.v1_16_R1.enchantments.CraftEnchantment.getRaw(enchantment).g())
        }
    }

    /**
     * 表现形式与 [Enchantment] 接近，仅转换为 NMS 类型的方法不同。
     */
    @Suppress("UNCHECKED_CAST")
    override fun getLocaleKey(potionEffectType: PotionEffectType?): LocaleKey {
        if (potionEffectType == null) {
            return LocaleKey("D", "null")
        }
        val descriptionId = if (MinecraftVersion.isUniversal) {
            // 1.17
            // 继续使用 fromId
            if (MinecraftVersion.isEqual(MinecraftVersion.V1_17)) {
                val registry = mobEffectIRegistry as net.minecraft.server.v1_16_R1.Registry<Any>
                registry.fromId(potionEffectType.id)!!.invokeMethod<String>("c", remap = false)
            }
            // 1.18 ... 1.20
            // fromId -> byId
            else {
                val registry = runCatching { mobEffectBuiltInRegistries }.getOrElse { mobEffectIRegistry }
                registry as net.minecraft.core.Registry<Any>
                registry.byId(potionEffectType.id)!!.invokeMethod<String>("getDescriptionId")
            }
        }
        // 1.13+ 开始使用 SystemUtils.a("effect", IRegistry.MOB_EFFECT.getKey(this)) 获取 key
        else if (MinecraftVersion.isIn(MinecraftVersion.V1_13..MinecraftVersion.V1_16)) {
            net.minecraft.server.v1_13_R2.MobEffectList.fromId(potionEffectType.id)!!.c()
        }
        // 1.13- 方法相对原始
        else if (MinecraftVersion.isIn(MinecraftVersion.V1_9..MinecraftVersion.V1_12)) {
            net.minecraft.server.v1_12_R1.MobEffectList.fromId(potionEffectType.id)!!.a()
        }
        // 1.8
        else {
            net.minecraft.server.v1_8_R3.MobEffectList.byId[potionEffectType.id].a()
        }
        return LocaleKey("N", descriptionId!!)
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
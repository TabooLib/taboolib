# bukkit-nms-component 设计文档

> 状态：草案 | 版本：v0.1 | 日期：2026-02-19

## 1. 概述

### 1.1 目标

为 Minecraft 1.20.5+ 引入的 Data Component 系统提供类型安全的跨版本封装。

- 镜像 NMS 的 `DataComponentType<T>` 设计，提供 `ComposedType<T>` 描述符
- 通过 `ComposedItem` 代理包装 NMS ItemStack，所有组件暴露为 Kotlin 属性委托
- 通过 `ComposedEntity` 代理包装 Entity，统一实体组件访问
- 低版本自动降级到 NBT 操作，不可降级时警告
- 支持序列化/反序列化，支持插件自定义扩展

### 1.2 设计原则

- **代理而非克隆**：`ComposedItem` 持有 NMS 副本，修改在副本上进行，由用户决定写回方式
- **属性委托**：每个组件是一行 `by composed(...)` 声明，Handler 逻辑完全隐藏
- **版本透明**：用户代码不感知版本差异，Handler 内部通过 nmsProxy/dynamic 适配
- **不可变复合类型**：复合组件（food、unbreakable 等）用 data class 表达，修改通过 `copy()` 产生新实例

### 1.3 依赖关系

```
bukkit-nms-component (API + 低版本实现)
├── 依赖 bukkit-nms (MinecraftVersion, nmsProxy, dynamic)
├── 依赖 bukkit-nms-tag (ItemTag, 用于 Legacy Handler)
│
└── bukkit-nms-component-modern (1.20.5+ NMS 实现)
    └── 依赖 bukkit-nms-component
```

## 2. 架构总览

```
┌─────────────────────────────────────────────────────┐
│                     用户代码                          │
│  val c = item.getComposedItem()                      │
│  c.customName = "§6Excalibur"                        │
│  c.saveTo(item)                                      │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│              ComposedItem (代理层)                     │
│  var customName by composed(ComposedTypes.CUSTOM_NAME)│
│  var damage     by composed(ComposedTypes.DAMAGE, 0)  │
│  内部持有 NMS ItemStack 副本                           │
└──────────────────────┬──────────────────────────────┘
                       │ 属性委托调用
┌──────────────────────▼──────────────────────────────┐
│           ComposedType<T> (组件描述符)                 │
│  id / translator / sinceVersion                      │
│  handler = version >= 12005 ? modern : legacy        │
└──────────────────────┬──────────────────────────────┘
                       │ 版本分发
              ┌────────┴────────┐
              ▼                 ▼
┌──────────────────┐  ┌──────────────────┐
│  ModernHandler   │  │  LegacyHandler   │
│  (nmsProxy/      │  │  (ItemTag NBT    │
│   dynamic)       │  │   路径操作)       │
│  1.20.5+         │  │  1.20.4-         │
└──────────────────┘  └──────────────────┘
```

## 3. 核心类型设计

### 3.1 ComponentHandler\<T\> — 版本适配器接口

```kotlin
/**
 * 组件处理器，直接操作 NMS 对象
 * Modern 实现通过 nmsProxy，Legacy 实现通过 ItemTag
 */
interface ComponentHandler<T> {
    fun get(nmsItem: Any): T?
    fun set(nmsItem: Any, value: T)
    fun remove(nmsItem: Any)
    fun has(nmsItem: Any): Boolean

    /** 便捷方法：value 为 null 时 remove */
    fun setOrRemove(nmsItem: Any, value: T?) {
        if (value != null) set(nmsItem, value) else remove(nmsItem)
    }
}
```

### 3.2 ComponentTranslator\<T\> — 翻译器

```kotlin
/**
 * 值类型 T 与 ItemTagData 之间的双向转换
 * 用途：序列化存储、低版本 NBT 操作、配置文件读写
 */
interface ComponentTranslator<T> {
    fun toTag(value: T): ItemTagData
    fun fromTag(data: ItemTagData): T
}
```

内置翻译器：

| 翻译器 | T 类型 | 说明 |
|---|---|---|
| `IntTranslator` | `Int` | 整数 |
| `BooleanTranslator` | `Boolean` | 布尔（存为 Byte 1/0） |
| `StringTranslator` | `String` | 字符串 |
| `StringListTranslator` | `List<String>` | 字符串列表 |
| `ItemTagTranslator` | `ItemTag` | 直接透传 ItemTag |
| `EnchantmentMapTranslator` | `Map<String, Int>` | 附魔映射 |
| 各 data class 翻译器 | `FoodComponent` 等 | 复合类型 |

### 3.3 ComposedType\<T\> — 组件描述符

```kotlin
/**
 * 对标 NMS DataComponentType<T>
 * 持有组件元信息 + 版本适配的 Handler
 */
class ComposedType<T>(
    val id: String,
    val translator: ComponentTranslator<T>,
    val sinceVersion: Int = 0
) {
    internal var modernHandler: ComponentHandler<T>? = null
    internal var legacyHandler: ComponentHandler<T>? = null

    internal val handler: ComponentHandler<T>? by unsafeLazy {
        when {
            MinecraftVersion.versionId >= 12005 -> modernHandler
            sinceVersion > 0 && MinecraftVersion.versionId < sinceVersion -> {
                warning("Component $id requires version $sinceVersion+, current: ${MinecraftVersion.versionId}")
                null
            }
            else -> legacyHandler
        }
    }

    /** 序列化 */
    fun serialize(value: T): ItemTagData = translator.toTag(value)

    /** 反序列化 */
    fun deserialize(data: ItemTagData): T = translator.fromTag(data)
}
```

### 3.4 ComposedType DSL 构建器

```kotlin
class ComposedTypeBuilder<T>(val id: String) {
    var translator: ComponentTranslator<T>? = null
    var sinceVersion: Int = 0
    private var modernClass: String? = null
    private var legacyHandler: ComponentHandler<T>? = null
    private var legacyPathResolver: ((Int) -> String?)? = null

    fun translator(t: ComponentTranslator<T>) { translator = t }
    fun since(version: Int) { sinceVersion = version }
    fun modern(className: String) { modernClass = className }
    fun legacy(path: String) {
        legacyHandler = LegacyNBTHandler(path, translator!!)
    }
    fun legacy(resolver: (version: Int) -> String?) {
        legacyPathResolver = resolver
    }
    fun legacy(handler: ComponentHandler<T>) {
        legacyHandler = handler
    }

    fun build(): ComposedType<T> {
        val type = ComposedType(id, translator!!, sinceVersion)
        type.modernHandler = modernClass?.let { nmsProxy(it) }
        type.legacyHandler = legacyPathResolver?.let { resolver ->
            val path = resolver(MinecraftVersion.versionId)
            path?.let { LegacyNBTHandler(it, translator!!) }
        } ?: legacyHandler
        return type
    }
}

fun <T> composedType(id: String, block: ComposedTypeBuilder<T>.() -> Unit): ComposedType<T> {
    return ComposedTypeBuilder<T>(id).apply(block).build()
}
```

### 3.5 属性委托

```kotlin
/**
 * 非空属性委托（有默认值）
 */
class ComponentDelegate<T>(
    val type: ComposedType<T>,
    val default: T
) : ReadWriteProperty<ComposedItem, T> {

    override fun getValue(thisRef: ComposedItem, property: KProperty<*>): T {
        return type.handler?.get(thisRef.nmsItem) ?: default
    }

    override fun setValue(thisRef: ComposedItem, property: KProperty<*>, value: T) {
        type.handler?.set(thisRef.nmsItem, value)
    }
}

/**
 * 可空属性委托（无默认值）
 */
class NullableComponentDelegate<T>(
    val type: ComposedType<T>
) : ReadWriteProperty<ComposedItem, T?> {

    override fun getValue(thisRef: ComposedItem, property: KProperty<*>): T? {
        return type.handler?.get(thisRef.nmsItem)
    }

    override fun setValue(thisRef: ComposedItem, property: KProperty<*>, value: T?) {
        type.handler?.setOrRemove(thisRef.nmsItem, value)
    }
}

// DSL 函数
fun <T> composed(type: ComposedType<T>): NullableComponentDelegate<T> =
    NullableComponentDelegate(type)

fun <T> composed(type: ComposedType<T>, default: T): ComponentDelegate<T> =
    ComponentDelegate(type, default)
```

## 4. ComposedItem 设计

### 4.1 类定义

```kotlin
class ComposedItem(internal val nmsItem: Any /* NMS ItemStack */) {

    // ══════════ 基础属性 ══════════
    var customName: String?         by composed(ComposedTypes.CUSTOM_NAME)
    var itemName: String?           by composed(ComposedTypes.ITEM_NAME)
    var lore: List<String>          by composed(ComposedTypes.LORE, emptyList())
    var damage: Int                 by composed(ComposedTypes.DAMAGE, 0)
    var maxDamage: Int              by composed(ComposedTypes.MAX_DAMAGE, 0)
    var maxStackSize: Int           by composed(ComposedTypes.MAX_STACK_SIZE, 64)
    var repairCost: Int             by composed(ComposedTypes.REPAIR_COST, 0)
    var customModelData: Int        by composed(ComposedTypes.CUSTOM_MODEL_DATA, 0)
    var rarity: ItemRarity          by composed(ComposedTypes.RARITY, ItemRarity.COMMON)

    // ══════════ 布尔/标记属性 ══════════
    var isUnbreakable: Boolean      by composed(ComposedTypes.UNBREAKABLE, false)
    var hideTooltip: Boolean        by composed(ComposedTypes.HIDE_TOOLTIP, false)
    var hideAdditionalTooltip: Boolean by composed(ComposedTypes.HIDE_ADDITIONAL_TOOLTIP, false)
    var enchantmentGlint: Boolean?  by composed(ComposedTypes.ENCHANTMENT_GLINT_OVERRIDE)
    var isFireResistant: Boolean    by composed(ComposedTypes.FIRE_RESISTANT, false)

    // ══════════ 集合属性 ══════════
    var enchantments: Map<String, Int>       by composed(ComposedTypes.ENCHANTMENTS, emptyMap())
    var storedEnchantments: Map<String, Int>  by composed(ComposedTypes.STORED_ENCHANTMENTS, emptyMap())
    var canBreak: List<String>               by composed(ComposedTypes.CAN_BREAK, emptyList())
    var canPlaceOn: List<String>             by composed(ComposedTypes.CAN_PLACE_ON, emptyList())

    // ══════════ 复合属性（不可变 data class） ══════════
    var food: FoodComponent?                 by composed(ComposedTypes.FOOD)
    var consumable: ConsumableComponent?     by composed(ComposedTypes.CONSUMABLE)
    var tool: ToolComponent?                 by composed(ComposedTypes.TOOL)
    var attributeModifiers: AttributeModifiersComponent?  by composed(ComposedTypes.ATTRIBUTE_MODIFIERS)
    var trim: ArmorTrimComponent?            by composed(ComposedTypes.TRIM)
    var dyedColor: DyedColorComponent?       by composed(ComposedTypes.DYED_COLOR)
    var customData: ItemTag?                 by composed(ComposedTypes.CUSTOM_DATA)

    // ══════════ 容器/特殊属性 ══════════
    var potionContents: PotionContentsComponent?     by composed(ComposedTypes.POTION_CONTENTS)
    var writableBookContent: WritableBookComponent?  by composed(ComposedTypes.WRITABLE_BOOK_CONTENT)
    var writtenBookContent: WrittenBookComponent?    by composed(ComposedTypes.WRITTEN_BOOK_CONTENT)
    var fireworks: FireworksComponent?               by composed(ComposedTypes.FIREWORKS)
    var profile: ProfileComponent?                   by composed(ComposedTypes.PROFILE)
    var bannerPatterns: List<BannerPatternEntry>      by composed(ComposedTypes.BANNER_PATTERNS, emptyList())
    var lodestoneTracker: LodestoneTrackerComponent? by composed(ComposedTypes.LODESTONE_TRACKER)
    var bundleContents: List<ItemStack>              by composed(ComposedTypes.BUNDLE_CONTENTS, emptyList())
    var container: List<ItemStack>                   by composed(ComposedTypes.CONTAINER, emptyList())

    // ══════════ 泛型访问（自定义扩展） ══════════
    fun <T> get(type: ComposedType<T>): T? = type.handler?.get(nmsItem)
    fun <T> set(type: ComposedType<T>, value: T) { type.handler?.set(nmsItem, value) }
    fun <T> remove(type: ComposedType<T>) { type.handler?.remove(nmsItem) }
    fun <T> has(type: ComposedType<T>): Boolean = type.handler?.has(nmsItem) ?: false

    // ══════════ 保存 ══════════

    /** 原地修改：将所有变更写回原物品 */
    fun saveTo(item: ItemStack) {
        val newItem = NMSItemTag.asBukkitCopy(nmsItem)
        item.type = newItem.type
        item.amount = newItem.amount
        item.itemMeta = newItem.itemMeta
    }

    /** 生成新物品 */
    fun toItemStack(): ItemStack {
        return NMSItemTag.asBukkitCopy(nmsItem)
    }
}
```

### 4.2 入口扩展函数

```kotlin
/** 获取物品的组件代理 */
fun ItemStack.getComposedItem(): ComposedItem {
    return ComposedItem(NMSItemTag.asNMSCopy(this))
}
```

### 4.3 使用示例

```kotlin
// 基本读写
val composed = item.getComposedItem()
composed.customName = "§6Excalibur"
composed.lore = listOf("§7A legendary sword", "§5Damage: +100")
composed.isUnbreakable = true
composed.damage = 0

// 复合类型修改
composed.food = FoodComponent(nutrition = 5, saturation = 0.6f)
composed.food = composed.food?.copy(canAlwaysEat = true)

// 集合操作
composed.enchantments = composed.enchantments + ("minecraft:sharpness" to 5)
composed.lore = composed.lore + "§7New line"

// 保存
composed.saveTo(item)          // 写回原物品
val newItem = composed.toItemStack()  // 生成新物品

// 自定义扩展
val myData = composed.get(MyPlugin.MY_COMPONENT)
composed.set(MyPlugin.MY_COMPONENT, MyData("hello"))
```

## 5. ComposedEntity 设计

### 5.1 类定义

```kotlin
class ComposedEntity(private val entity: Entity) {

    // ══════════ 通用属性（全版本，通过 Bukkit API） ══════════
    var customName: String?
        get() = entity.customName
        set(value) { entity.customName = value }

    var isCustomNameVisible: Boolean
        get() = entity.isCustomNameVisible
        set(value) { entity.isCustomNameVisible = value }

    var isGlowing: Boolean
        get() = entity.isGlowing
        set(value) { entity.isGlowing = value }

    var isSilent: Boolean
        get() = entity.isSilent
        set(value) { entity.isSilent = value }

    var isInvulnerable: Boolean
        get() = entity.isInvulnerable
        set(value) { entity.isInvulnerable = value }

    var hasGravity: Boolean
        get() = entity.hasGravity()
        set(value) { entity.setGravity(value) }

    // ══════════ LivingEntity 属性 ══════════
    var health: Double
        get() = (entity as? LivingEntity)?.health ?: 0.0
        set(value) { (entity as? LivingEntity)?.health = value }

    var maxHealth: Double
        get() = (entity as? LivingEntity)?.maxHealth ?: 0.0
        set(value) { (entity as? LivingEntity)?.maxHealth = value }

    var isAI: Boolean
        get() = (entity as? LivingEntity)?.hasAI() ?: false
        set(value) { (entity as? LivingEntity)?.setAI(value) }

    var isCollidable: Boolean
        get() = (entity as? LivingEntity)?.isCollidable ?: true
        set(value) { (entity as? LivingEntity)?.isCollidable = value }

    // ══════════ NMS 组件（高版本，通过 Handler） ══════════
    // 未来 Minecraft 可能为实体引入 DataComponent 系统
    // 届时通过 ComposedType + Handler 扩展

    fun <T> get(type: ComposedType<T>): T? = type.handler?.get(getNmsEntity())
    fun <T> set(type: ComposedType<T>, value: T) { type.handler?.set(getNmsEntity(), value) }

    private fun getNmsEntity(): Any {
        return entity::class.java.getMethod("getHandle").invoke(entity)
    }
}

fun Entity.getComposedEntity(): ComposedEntity {
    return ComposedEntity(this)
}
```

### 5.2 与 ComposedItem 的区别

| | ComposedItem | ComposedEntity |
|---|---|---|
| 内部对象 | NMS ItemStack 副本 | Bukkit Entity 引用 |
| 修改方式 | 修改副本，手动保存 | 直接生效（Bukkit API 原地修改） |
| saveTo/toItemStack | 需要 | 不需要 |
| 版本适配 | Modern/Legacy Handler | 大部分走 Bukkit API，少量走 NMS |

## 6. 版本适配策略

### 6.1 Modern Handler（1.20.5+）

通过 `nmsProxy` 加载，运行在 NMS 环境中，直接操作 `DataComponents`。

```kotlin
// bukkit-nms-component-modern 模块
class ModernCustomNameHandler : ComponentHandler<String> {

    override fun get(nmsItem: Any): String? {
        val nms = nmsItem as net.minecraft.world.item.ItemStack
        val component = nms.get(DataComponents.CUSTOM_NAME) ?: return null
        // NMS Component → JSON 字符串
        return GsonComponentSerializer.gson().serialize(
            PaperAdventure.asAdventure(component)
        )
    }

    override fun set(nmsItem: Any, value: String) {
        val nms = nmsItem as net.minecraft.world.item.ItemStack
        // 支持 § 颜色码和 JSON 格式
        val nmsComponent = parseToNMSComponent(value)
        nms.set(DataComponents.CUSTOM_NAME, nmsComponent)
    }

    override fun remove(nmsItem: Any) {
        (nmsItem as net.minecraft.world.item.ItemStack).remove(DataComponents.CUSTOM_NAME)
    }

    override fun has(nmsItem: Any): Boolean {
        return (nmsItem as net.minecraft.world.item.ItemStack).has(DataComponents.CUSTOM_NAME)
    }
}
```

版本内部差异通过 `dynamic` 处理：

```kotlin
// 1.20.5 vs 1.21.5 方法签名不同时
val value = if (versionId >= 12105) {
    dynamic(DynamicOpcode.INVOKEVIRTUAL, "...#value()...", nmsObj)
} else {
    dynamic(DynamicOpcode.INVOKEVIRTUAL, "...#getAsInt()...", nmsObj)
}
```

### 6.2 Legacy Handler（1.20.4 及以下）

通用的 NBT 路径处理器，通过 ItemTag 操作：

```kotlin
class LegacyNBTHandler<T>(
    val path: String,
    val translator: ComponentTranslator<T>
) : ComponentHandler<T> {

    override fun get(nmsItem: Any): T? {
        val item = NMSItemTag.asBukkitCopy(nmsItem)
        val tag = item.getItemTag()
        return tag.getDeep(path)?.let { translator.fromTag(it) }
    }

    override fun set(nmsItem: Any, value: T) {
        val item = NMSItemTag.asBukkitCopy(nmsItem)
        val tag = item.getItemTag()
        tag.putDeep(path, translator.toTag(value))
        val newItem = tag.saveTo(item)
        // 将修改后的 Bukkit 物品转回 NMS 并拷贝数据
        NMSItemTagHelper.copyNmsItemData(
            NMSItemTag.asNMSCopy(newItem), nmsItem
        )
    }

    override fun remove(nmsItem: Any) {
        val item = NMSItemTag.asBukkitCopy(nmsItem)
        val tag = item.getItemTag()
        tag.removeDeep(path)
        val newItem = tag.saveTo(item)
        NMSItemTagHelper.copyNmsItemData(
            NMSItemTag.asNMSCopy(newItem), nmsItem
        )
    }

    override fun has(nmsItem: Any): Boolean {
        val item = NMSItemTag.asBukkitCopy(nmsItem)
        return item.getItemTag().getDeep(path) != null
    }
}
```

### 6.3 Unsupported Handler

```kotlin
class UnsupportedHandler<T>(val reason: String) : ComponentHandler<T> {
    override fun get(nmsItem: Any): T? {
        warning("Component not available: $reason")
        return null
    }
    override fun set(nmsItem: Any, value: T) = warning("Component not available: $reason")
    override fun remove(nmsItem: Any) = warning("Component not available: $reason")
    override fun has(nmsItem: Any): Boolean = false
}
```

### 6.4 版本降级对照表

| 组件 | 1.20.5+ | 低版本 NBT 路径 | 备注 |
|---|---|---|---|
| custom_name | DataComponents.CUSTOM_NAME | `display.Name` | 全版本 |
| lore | DataComponents.LORE | `display.Lore` | 全版本 |
| damage | DataComponents.DAMAGE | `Damage` | 全版本 |
| enchantments | DataComponents.ENCHANTMENTS | `Enchantments` / `ench` | 1.13 前用 `ench` |
| unbreakable | DataComponents.UNBREAKABLE | `Unbreakable` | 全版本 |
| repair_cost | DataComponents.REPAIR_COST | `RepairCost` | 全版本 |
| custom_model_data | DataComponents.CUSTOM_MODEL_DATA | `CustomModelData` | 1.14+ |
| custom_data | DataComponents.CUSTOM_DATA | 整个 NBT Tag | 全版本 |
| can_break | DataComponents.CAN_BREAK | `CanDestroy` | 全版本 |
| can_place_on | DataComponents.CAN_PLACE_ON | `CanPlaceOn` | 全版本 |
| hide_flags | DataComponents.HIDE_* | `HideFlags` | 低版本为位掩码 |
| attribute_modifiers | DataComponents.ATTRIBUTE_MODIFIERS | `AttributeModifiers` | 全版本 |
| dyed_color | DataComponents.DYED_COLOR | `display.color` | 全版本 |
| potion_contents | DataComponents.POTION_CONTENTS | `Potion` + `CustomPotionEffects` | 全版本 |
| stored_enchantments | DataComponents.STORED_ENCHANTMENTS | `StoredEnchantments` | 全版本 |
| food | DataComponents.FOOD | — | 1.20.5+ only |
| consumable | DataComponents.CONSUMABLE | — | 1.21.2+ only |
| tool | DataComponents.TOOL | — | 1.20.5+ only |
| trim | DataComponents.TRIM | — | 1.19.4+ (Bukkit API) |
| max_stack_size | DataComponents.MAX_STACK_SIZE | — | 1.20.5+ only |
| rarity | DataComponents.RARITY | — | 1.20.5+ only |
| fire_resistant | DataComponents.FIRE_RESISTANT | — | 1.20.5+ only |

## 7. 复合数据类型定义

所有复合组件用不可变 `data class` 表达，修改通过 `copy()` 产生新实例。

### 7.1 枚举类型

```kotlin
enum class ItemRarity { COMMON, UNCOMMON, RARE, EPIC }

enum class AttributeOperation { ADD_VALUE, ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL }

enum class EquipmentSlotGroup { ANY, MAINHAND, OFFHAND, HAND, FEET, LEGS, CHEST, HEAD, ARMOR, BODY }

enum class ConsumeAnimation { NONE, EAT, DRINK, BLOCK, BOW, SPEAR, CROSSBOW, SPYGLASS, TOOT_HORN, BRUSH }
```

### 7.2 复合数据类

```kotlin
data class FoodComponent(
    val nutrition: Int = 0,
    val saturation: Float = 0f,
    val canAlwaysEat: Boolean = false
)

data class ConsumableComponent(
    val consumeSeconds: Float = 1.6f,
    val animation: ConsumeAnimation = ConsumeAnimation.EAT,
    val hasConsumeParticles: Boolean = true
)

data class ToolComponent(
    val rules: List<ToolRule> = emptyList(),
    val defaultMiningSpeed: Float = 1.0f,
    val damagePerBlock: Int = 1
)

data class ToolRule(
    val blocks: List<String>,       // 方块 ID 或标签
    val speed: Float? = null,
    val correctForDrops: Boolean? = null
)

data class AttributeModifiersComponent(
    val modifiers: List<AttributeEntry> = emptyList(),
    val showInTooltip: Boolean = true
)

data class AttributeEntry(
    val attribute: String,          // "minecraft:generic.attack_damage"
    val name: String,
    val amount: Double,
    val operation: AttributeOperation,
    val slot: EquipmentSlotGroup = EquipmentSlotGroup.ANY
)

data class ArmorTrimComponent(
    val material: String,           // "minecraft:iron"
    val pattern: String,            // "minecraft:sentry"
    val showInTooltip: Boolean = true
)

data class DyedColorComponent(
    val color: Int,                 // RGB
    val showInTooltip: Boolean = true
)

data class PotionContentsComponent(
    val potion: String? = null,     // "minecraft:healing"
    val customColor: Int? = null,
    val customEffects: List<PotionEffect> = emptyList()
)

data class PotionEffect(
    val id: String,                 // "minecraft:speed"
    val amplifier: Int = 0,
    val duration: Int = 200,
    val ambient: Boolean = false,
    val showParticles: Boolean = true,
    val showIcon: Boolean = true
)

data class WritableBookComponent(
    val pages: List<String> = emptyList()
)

data class WrittenBookComponent(
    val title: String = "",
    val author: String = "",
    val generation: Int = 0,
    val pages: List<String> = emptyList(),  // JSON Component 字符串
    val resolved: Boolean = false
)

data class FireworksComponent(
    val flightDuration: Int = 0,
    val explosions: List<FireworkExplosion> = emptyList()
)

data class FireworkExplosion(
    val shape: String = "small_ball",  // small_ball, large_ball, star, creeper, burst
    val colors: List<Int> = emptyList(),
    val fadeColors: List<Int> = emptyList(),
    val hasTrail: Boolean = false,
    val hasTwinkle: Boolean = false
)

data class ProfileComponent(
    val name: String? = null,
    val uuid: UUID? = null,
    val properties: Map<String, String> = emptyMap()  // 主要是 textures
)

data class BannerPatternEntry(
    val pattern: String,            // "minecraft:stripe_bottom"
    val color: String               // "red"
)

data class LodestoneTrackerComponent(
    val target: LodestoneTarget? = null,
    val tracked: Boolean = true
)

data class LodestoneTarget(
    val dimension: String,          // "minecraft:overworld"
    val x: Int, val y: Int, val z: Int
)
```

## 8. 序列化/反序列化

每个 `ComposedType<T>` 自带 `ComponentTranslator<T>`，支持 `T ↔ ItemTagData` 双向转换。

### 8.1 单组件序列化

```kotlin
// 序列化
val food = composed.food!!
val data: ItemTagData = ComposedTypes.FOOD.serialize(food)
val json: String = data.saveToString()

// 反序列化
val restored: FoodComponent = ComposedTypes.FOOD.deserialize(ItemTag.fromJson(json))
composed.food = restored
```

### 8.2 物品快照（全组件序列化）

```kotlin
/** 将物品的所有已知组件序列化为 Map */
fun ComposedItem.snapshot(): ItemTag {
    val tag = ItemTag.empty()
    ComposedRegistry.all().forEach { type ->
        @Suppress("UNCHECKED_CAST")
        val composed = type as ComposedType<Any>
        val value = composed.handler?.get(nmsItem) ?: return@forEach
        tag[type.id] = composed.serialize(value)
    }
    return tag
}

/** 从快照恢复 */
fun ComposedItem.restore(snapshot: ItemTag) {
    snapshot.forEach { (id, data) ->
        @Suppress("UNCHECKED_CAST")
        val type = ComposedRegistry.get(id) as? ComposedType<Any> ?: return@forEach
        val value = type.deserialize(data)
        type.handler?.set(nmsItem, value)
    }
}
```

## 9. 自定义扩展机制

### 9.1 ComposedRegistry

```kotlin
object ComposedRegistry {

    private val registry = ConcurrentHashMap<String, ComposedType<*>>()

    fun register(type: ComposedType<*>) {
        registry[type.id] = type
    }

    fun unregister(id: String) {
        registry.remove(id)
    }

    fun get(id: String): ComposedType<*>? = registry[id]

    fun all(): Collection<ComposedType<*>> = registry.values

    fun has(id: String): Boolean = registry.containsKey(id)
}
```

### 9.2 插件自定义组件示例

```kotlin
// 定义
object MyPlugin {
    val SOUL_BOUND = composedType<Boolean>("myplugin:soul_bound") {
        translator(BooleanTranslator)
        legacy("MyPlugin.SoulBound")
        modern("com.myplugin.handler.ModernSoulBoundHandler")
    }

    val CUSTOM_STATS = composedType<Map<String, Double>>("myplugin:stats") {
        translator(StringDoubleMapTranslator)
        legacy("MyPlugin.Stats")
        modern("com.myplugin.handler.ModernStatsHandler")
    }
}

// 注册（在插件 onEnable 中）
ComposedRegistry.register(MyPlugin.SOUL_BOUND)
ComposedRegistry.register(MyPlugin.CUSTOM_STATS)

// 使用
val composed = item.getComposedItem()
val isSoulBound = composed.get(MyPlugin.SOUL_BOUND) ?: false
composed.set(MyPlugin.CUSTOM_STATS, mapOf("attack" to 100.0, "defense" to 50.0))
composed.saveTo(item)
```

## 10. ComposedTypes 静态字段定义

```kotlin
object ComposedTypes {

    // ══════════ 基础 ══════════
    val CUSTOM_NAME = composedType<String>("minecraft:custom_name") {
        translator(JsonComponentTranslator)
        legacy("display.Name")
        modern("{package}.handler.ModernCustomNameHandler")
    }

    val ITEM_NAME = composedType<String>("minecraft:item_name") {
        translator(JsonComponentTranslator)
        since(12005)
        modern("{package}.handler.ModernItemNameHandler")
    }

    val LORE = composedType<List<String>>("minecraft:lore") {
        translator(JsonComponentListTranslator)
        legacy("display.Lore")
        modern("{package}.handler.ModernLoreHandler")
    }

    val DAMAGE = composedType<Int>("minecraft:damage") {
        translator(IntTranslator)
        legacy("Damage")
        modern("{package}.handler.ModernDamageHandler")
    }

    val MAX_DAMAGE = composedType<Int>("minecraft:max_damage") {
        translator(IntTranslator)
        since(12005)
        modern("{package}.handler.ModernMaxDamageHandler")
    }

    val MAX_STACK_SIZE = composedType<Int>("minecraft:max_stack_size") {
        translator(IntTranslator)
        since(12005)
        modern("{package}.handler.ModernMaxStackSizeHandler")
    }

    val REPAIR_COST = composedType<Int>("minecraft:repair_cost") {
        translator(IntTranslator)
        legacy("RepairCost")
        modern("{package}.handler.ModernRepairCostHandler")
    }

    val CUSTOM_MODEL_DATA = composedType<Int>("minecraft:custom_model_data") {
        translator(IntTranslator)
        legacy { v -> if (v >= 11400) "CustomModelData" else null }
        modern("{package}.handler.ModernCustomModelDataHandler")
    }

    val RARITY = composedType<ItemRarity>("minecraft:rarity") {
        translator(RarityTranslator)
        since(12005)
        modern("{package}.handler.ModernRarityHandler")
    }

    // ══════════ 布尔/标记 ══════════
    val UNBREAKABLE = composedType<Boolean>("minecraft:unbreakable") {
        translator(BooleanTranslator)
        legacy("Unbreakable")
        modern("{package}.handler.ModernUnbreakableHandler")
    }

    val HIDE_TOOLTIP = composedType<Boolean>("minecraft:hide_tooltip") {
        translator(BooleanTranslator)
        since(12005)
        modern("{package}.handler.ModernHideTooltipHandler")
    }

    val HIDE_ADDITIONAL_TOOLTIP = composedType<Boolean>("minecraft:hide_additional_tooltip") {
        translator(BooleanTranslator)
        since(12005)
        modern("{package}.handler.ModernHideAdditionalTooltipHandler")
    }

    val ENCHANTMENT_GLINT_OVERRIDE = composedType<Boolean>("minecraft:enchantment_glint_override") {
        translator(BooleanTranslator)
        since(12005)
        modern("{package}.handler.ModernEnchantmentGlintHandler")
    }

    val FIRE_RESISTANT = composedType<Boolean>("minecraft:fire_resistant") {
        translator(BooleanTranslator)
        since(12005)
        modern("{package}.handler.ModernFireResistantHandler")
    }

    // ══════════ 集合 ══════════
    val ENCHANTMENTS = composedType<Map<String, Int>>("minecraft:enchantments") {
        translator(EnchantmentMapTranslator)
        legacy { v -> if (v >= 11300) "Enchantments" else "ench" }
        modern("{package}.handler.ModernEnchantmentsHandler")
    }

    val STORED_ENCHANTMENTS = composedType<Map<String, Int>>("minecraft:stored_enchantments") {
        translator(EnchantmentMapTranslator)
        legacy("StoredEnchantments")
        modern("{package}.handler.ModernStoredEnchantmentsHandler")
    }

    val CAN_BREAK = composedType<List<String>>("minecraft:can_break") {
        translator(StringListTranslator)
        legacy("CanDestroy")
        modern("{package}.handler.ModernCanBreakHandler")
    }

    val CAN_PLACE_ON = composedType<List<String>>("minecraft:can_place_on") {
        translator(StringListTranslator)
        legacy("CanPlaceOn")
        modern("{package}.handler.ModernCanPlaceOnHandler")
    }

    // ══════════ 复合 ══════════
    val FOOD = composedType<FoodComponent>("minecraft:food") {
        translator(FoodTranslator)
        since(12005)
        modern("{package}.handler.ModernFoodHandler")
    }

    val CONSUMABLE = composedType<ConsumableComponent>("minecraft:consumable") {
        translator(ConsumableTranslator)
        since(12102)  // 1.21.2+
        modern("{package}.handler.ModernConsumableHandler")
    }

    val TOOL = composedType<ToolComponent>("minecraft:tool") {
        translator(ToolTranslator)
        since(12005)
        modern("{package}.handler.ModernToolHandler")
    }

    val ATTRIBUTE_MODIFIERS = composedType<AttributeModifiersComponent>("minecraft:attribute_modifiers") {
        translator(AttributeModifiersTranslator)
        legacy("AttributeModifiers")
        modern("{package}.handler.ModernAttributeModifiersHandler")
    }

    val TRIM = composedType<ArmorTrimComponent>("minecraft:trim") {
        translator(ArmorTrimTranslator)
        since(11904)  // 1.19.4+
        modern("{package}.handler.ModernTrimHandler")
    }

    val DYED_COLOR = composedType<DyedColorComponent>("minecraft:dyed_color") {
        translator(DyedColorTranslator)
        legacy("display.color")
        modern("{package}.handler.ModernDyedColorHandler")
    }

    val CUSTOM_DATA = composedType<ItemTag>("minecraft:custom_data") {
        translator(ItemTagTranslator)
        legacy(LegacyCustomDataHandler())  // 低版本 = 整个 NBT
        modern("{package}.handler.ModernCustomDataHandler")
    }

    // ══════════ 容器/特殊 ══════════
    val POTION_CONTENTS = composedType<PotionContentsComponent>("minecraft:potion_contents") {
        translator(PotionContentsTranslator)
        legacy(LegacyPotionHandler())
        modern("{package}.handler.ModernPotionContentsHandler")
    }

    val WRITABLE_BOOK_CONTENT = composedType<WritableBookComponent>("minecraft:writable_book_content") {
        translator(WritableBookTranslator)
        legacy("pages")
        modern("{package}.handler.ModernWritableBookHandler")
    }

    val WRITTEN_BOOK_CONTENT = composedType<WrittenBookComponent>("minecraft:written_book_content") {
        translator(WrittenBookTranslator)
        legacy(LegacyWrittenBookHandler())
        modern("{package}.handler.ModernWrittenBookHandler")
    }

    val FIREWORKS = composedType<FireworksComponent>("minecraft:fireworks") {
        translator(FireworksTranslator)
        legacy("Fireworks")
        modern("{package}.handler.ModernFireworksHandler")
    }

    val PROFILE = composedType<ProfileComponent>("minecraft:profile") {
        translator(ProfileTranslator)
        legacy("SkullOwner")
        modern("{package}.handler.ModernProfileHandler")
    }

    val BANNER_PATTERNS = composedType<List<BannerPatternEntry>>("minecraft:banner_patterns") {
        translator(BannerPatternsTranslator)
        legacy("Patterns")
        modern("{package}.handler.ModernBannerPatternsHandler")
    }

    val LODESTONE_TRACKER = composedType<LodestoneTrackerComponent>("minecraft:lodestone_tracker") {
        translator(LodestoneTrackerTranslator)
        legacy(LegacyLodestoneHandler())
        modern("{package}.handler.ModernLodestoneTrackerHandler")
    }

    val BUNDLE_CONTENTS = composedType<List<ItemStack>>("minecraft:bundle_contents") {
        translator(ItemStackListTranslator)
        since(11700)  // 1.17+
        modern("{package}.handler.ModernBundleContentsHandler")
    }

    val CONTAINER = composedType<List<ItemStack>>("minecraft:container") {
        translator(ItemStackListTranslator)
        since(12005)
        modern("{package}.handler.ModernContainerHandler")
    }
}
```

## 11. 模块结构

```
module/bukkit-nms/bukkit-nms-component/
│
├── DESIGN.md                                    # 本文档
├── build.gradle.kts
│
├── src/main/kotlin/taboolib/module/nms/
│   │
│   │  ── 核心框架 ──
│   ├── ComponentHandler.kt                      # Handler 接口
│   ├── ComponentTranslator.kt                   # 翻译器接口
│   ├── ComposedType.kt                          # 组件描述符 + DSL 构建器
│   ├── ComposedItem.kt                          # 物品代理 + 属性委托
│   ├── ComposedEntity.kt                        # 实体代理
│   ├── ComposedRegistry.kt                      # 注册表
│   ├── ComponentDelegate.kt                     # 属性委托实现
│   │
│   │  ── 数据类型 ──
│   ├── component/
│   │   ├── ItemRarity.kt
│   │   ├── FoodComponent.kt
│   │   ├── ConsumableComponent.kt
│   │   ├── ToolComponent.kt
│   │   ├── AttributeModifiersComponent.kt
│   │   ├── ArmorTrimComponent.kt
│   │   ├── DyedColorComponent.kt
│   │   ├── PotionContentsComponent.kt
│   │   ├── WritableBookComponent.kt
│   │   ├── WrittenBookComponent.kt
│   │   ├── FireworksComponent.kt
│   │   ├── ProfileComponent.kt
│   │   ├── BannerPatternEntry.kt
│   │   └── LodestoneTrackerComponent.kt
│   │
│   │  ── 翻译器 ──
│   ├── translator/
│   │   ├── IntTranslator.kt
│   │   ├── BooleanTranslator.kt
│   │   ├── StringTranslator.kt
│   │   ├── JsonComponentTranslator.kt           # 处理 § 颜色码 ↔ JSON Component
│   │   ├── StringListTranslator.kt
│   │   ├── EnchantmentMapTranslator.kt
│   │   ├── ItemTagTranslator.kt
│   │   └── ... (各复合类型翻译器)
│   │
│   │  ── 低版本处理器 ──
│   ├── handler/
│   │   ├── LegacyNBTHandler.kt                  # 通用 NBT 路径处理器
│   │   ├── LegacyCustomDataHandler.kt           # 低版本 custom_data 特殊处理
│   │   ├── LegacyPotionHandler.kt               # 低版本药水特殊处理
│   │   ├── LegacyWrittenBookHandler.kt
│   │   ├── LegacyLodestoneHandler.kt
│   │   └── UnsupportedHandler.kt
│   │
│   │  ── 静态字段 ──
│   └── ComposedTypes.kt                         # 所有内置组件定义
│
└── bukkit-nms-component-modern/                  # 1.20.5+ NMS 实现
    ├── build.gradle.kts
    └── src/main/kotlin/taboolib/module/nms/handler/
        ├── ModernCustomNameHandler.kt
        ├── ModernLoreHandler.kt
        ├── ModernDamageHandler.kt
        ├── ModernEnchantmentsHandler.kt
        ├── ModernUnbreakableHandler.kt
        ├── ModernFoodHandler.kt
        ├── ModernConsumableHandler.kt
        ├── ModernToolHandler.kt
        ├── ModernAttributeModifiersHandler.kt
        ├── ModernTrimHandler.kt
        ├── ModernDyedColorHandler.kt
        ├── ModernCustomDataHandler.kt
        ├── ModernPotionContentsHandler.kt
        ├── ModernProfileHandler.kt
        ├── ModernFireworksHandler.kt
        ├── ModernBannerPatternsHandler.kt
        └── ... (其余 Modern Handler)
```

## 12. 实现计划

### Phase 1：核心框架

- [ ] `ComponentHandler` 接口
- [ ] `ComponentTranslator` 接口 + 基础翻译器（Int, Boolean, String, StringList）
- [ ] `ComposedType` + DSL 构建器
- [ ] `ComponentDelegate` + `NullableComponentDelegate`
- [ ] `ComposedItem` 骨架（saveTo / toItemStack / 泛型 get/set）
- [ ] `ComposedRegistry`
- [ ] `LegacyNBTHandler` 通用处理器
- [ ] `UnsupportedHandler`
- [ ] `build.gradle.kts` 模块配置

### Phase 2：基础组件

- [ ] custom_name (Modern + Legacy)
- [ ] lore (Modern + Legacy)
- [ ] damage (Modern + Legacy)
- [ ] enchantments (Modern + Legacy，含 1.13 前兼容)
- [ ] unbreakable (Modern + Legacy)
- [ ] custom_data (Modern + Legacy)
- [ ] custom_model_data (Modern + Legacy)
- [ ] repair_cost (Modern + Legacy)
- [ ] can_break / can_place_on (Modern + Legacy)

### Phase 3：复合组件

- [ ] food / consumable 数据类 + Handler
- [ ] tool 数据类 + Handler
- [ ] attribute_modifiers 数据类 + Handler
- [ ] potion_contents 数据类 + Handler
- [ ] dyed_color 数据类 + Handler
- [ ] trim 数据类 + Handler

### Phase 4：特殊组件

- [ ] profile (头颅)
- [ ] fireworks (烟花)
- [ ] banner_patterns (旗帜)
- [ ] written_book_content / writable_book_content (书本)
- [ ] lodestone_tracker (磁石)
- [ ] bundle_contents / container (容器)
- [ ] max_stack_size / max_damage / rarity / fire_resistant 等标记组件

### Phase 5：ComposedEntity

- [ ] ComposedEntity 基础属性（通过 Bukkit API）
- [ ] 泛型 get/set 支持
- [ ] 高版本实体组件扩展（如果 Mojang 引入）

### Phase 6：完善

- [ ] 序列化快照 snapshot / restore
- [ ] 迁移现有 `NMSItemTag` 中的 canBreak/canPlaceOn 到新系统
- [ ] 文档和使用示例
- [ ] 单元测试

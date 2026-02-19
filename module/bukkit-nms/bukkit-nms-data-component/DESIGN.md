# bukkit-nms-data-component 设计文档

> 状态：草案 | 版本：v0.2 | 日期：2026-02-19

## 1. 概述

### 1.1 目标

为 Minecraft 1.20.5+ 引入的 Data Component 系统提供类型安全的跨版本封装。

- 镜像 NMS 的 `DataComponentType<T>` 设计，提供 `ComposedType<T>` 描述符
- 通过 `ComposedItem` 克隆 NMS ItemStack，所有组件暴露为 Kotlin 属性委托
- 通过 `ComposedEntity` 封装 Entity，统一实体组件访问
- 低版本自动降级到 NBT 操作，不可降级时静默警告
- 支持序列化/反序列化，支持插件自定义扩展

### 1.2 设计原则

- **克隆而非代理**：`getComposedItem()` 时克隆一份 NMS ItemStack，所有修改在克隆体上进行，由用户决定写回方式（`saveTo` 或 `toItemStack`）
- **属性委托**：每个组件是一行 `by composed(...)` 声明，Transformer 逻辑完全隐藏
- **版本透明**：用户代码不感知版本差异，Transformer 内部通过 nmsProxy/dynamic 适配
- **不可变复合类型**：复合组件用 data class 表达，修改通过 `copy()` 产生新实例
- **静默降级**：不支持的版本返回 null/默认值 + warning 日志，不抛异常

### 1.3 依赖关系

```
bukkit-nms-data-component (API + 全版本实现)
├── 依赖 bukkit-nms (MinecraftVersion, nmsProxy, dynamic)
└── 依赖 bukkit-nms-tag (ItemTag, 用于低版本 NBT 操作)
```

> 不再拆分 modern 子模块。每个 Transformer 是单类，内部通过懒加载分支处理版本差异。

### 1.4 与旧方案的区别

| | 旧方案（Handler 拆分） | 新方案（Transformer 单类） |
|---|---|---|
| 版本适配 | ModernHandler + LegacyHandler 两个类 | 单个 Transformer 类，内部 lazy 分支 |
| ComposedType 职责 | 持有 modernHandler/legacyHandler | 纯描述符，不持有 Transformer |
| Transformer 管理 | 由 ComposedType 持有 | 独立管理，通过 TransformerRegistry 查找 |
| 入口行为 | 代理原 NMS 对象 | 克隆 NMS ItemStack，修改克隆体 |

## 2. 架构总览

```
┌─────────────────────────────────────────────────────┐
│                     用户代码                          │
│  val c = item.getComposedItem()   // 克隆 NMS 副本   │
│  c.customName = "§6Excalibur"                        │
│  c[ComposedTypes.DAMAGE] = 0                         │
│  c.saveTo(item)                                      │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│              ComposedItem (克隆层)                     │
│  var customName by composed(ComposedTypes.CUSTOM_NAME)│
│  var damage     by composed(ComposedTypes.DAMAGE, 0)  │
│  operator fun get/set/contains/minusAssign            │
│  内部持有克隆的 NMS ItemStack                          │
└──────────────────────┬──────────────────────────────┘
                       │ 属性委托 / 泛型方法
┌──────────────────────▼──────────────────────────────┐
│           ComposedType<T> (纯描述符)                   │
│  id / translator / sinceVersion                      │
│  不持有 Transformer，仅描述组件元信息                    │
└──────────────────────┬──────────────────────────────┘
                       │ 通过 TransformerRegistry 查找
┌──────────────────────▼──────────────────────────────┐
│       ComponentTransformer<T> (单类，内部分支)          │
│  内部 lazy { 根据版本选择 modern/legacy 逻辑 }         │
│  get / set / remove / has                            │
│  modern: nmsProxy/dynamic 操作 DataComponents         │
│  legacy: ItemTag NBT 路径操作                         │
│  unsupported: warning + null                         │
└─────────────────────────────────────────────────────┘
```

## 3. 核心类型设计

### 3.1 ComponentTransformer\<T\> — 版本适配器

单类设计，内部通过懒加载分支处理版本差异。不再拆分 Modern/Legacy 两个类。

```kotlin
/**
 * 组件转换器，直接操作 NMS 对象
 * 每个 Transformer 是独立的单类，内部处理版本分支
 */
abstract class ComponentTransformer<T>(
    val typeId: String,
    val sinceVersion: Int = 0
) {

    /** 版本策略，懒加载确定 */
    private val strategy: Strategy by unsafeLazy {
        when {
            MinecraftVersion.versionId >= 12005 -> Strategy.MODERN
            sinceVersion > 0 && MinecraftVersion.versionId < sinceVersion -> {
                warning("Component $typeId requires version $sinceVersion+")
                Strategy.UNSUPPORTED
            }
            else -> Strategy.LEGACY
        }
    }

    // ══════════ 公开 API ══════════

    fun get(nmsItem: Any): T? = when (strategy) {
        Strategy.MODERN -> getModern(nmsItem)
        Strategy.LEGACY -> getLegacy(nmsItem)
        Strategy.UNSUPPORTED -> null
    }

    fun set(nmsItem: Any, value: T) = when (strategy) {
        Strategy.MODERN -> setModern(nmsItem, value)
        Strategy.LEGACY -> setLegacy(nmsItem, value)
        Strategy.UNSUPPORTED -> warning("Cannot set $typeId: unsupported version")
    }

    fun remove(nmsItem: Any) = when (strategy) {
        Strategy.MODERN -> removeModern(nmsItem)
        Strategy.LEGACY -> removeLegacy(nmsItem)
        Strategy.UNSUPPORTED -> warning("Cannot remove $typeId: unsupported version")
    }

    fun has(nmsItem: Any): Boolean = when (strategy) {
        Strategy.MODERN -> hasModern(nmsItem)
        Strategy.LEGACY -> hasLegacy(nmsItem)
        Strategy.UNSUPPORTED -> false
    }

    fun setOrRemove(nmsItem: Any, value: T?) {
        if (value != null) set(nmsItem, value) else remove(nmsItem)
    }

    // ══════════ 子类实现 ══════════

    /** 1.20.5+ 通过 nmsProxy/dynamic 操作 DataComponents */
    protected abstract fun getModern(nmsItem: Any): T?
    protected abstract fun setModern(nmsItem: Any, value: T)
    protected abstract fun removeModern(nmsItem: Any)
    protected abstract fun hasModern(nmsItem: Any): Boolean

    /** 1.20.4- 通过 ItemTag NBT 操作 */
    protected abstract fun getLegacy(nmsItem: Any): T?
    protected abstract fun setLegacy(nmsItem: Any, value: T)
    protected abstract fun removeLegacy(nmsItem: Any)
    protected abstract fun hasLegacy(nmsItem: Any): Boolean

    private enum class Strategy { MODERN, LEGACY, UNSUPPORTED }
}
```

便捷基类 — 基于 NBT 路径的通用低版本实现：

```kotlin
/**
 * 简化 Transformer 实现：Legacy 部分通过 NBT 路径 + Translator 自动处理
 * 子类只需实现 Modern 部分
 */
abstract class NBTPathTransformer<T>(
    typeId: String,
    private val nbtPath: String,
    private val translator: ComponentTranslator<T>,
    sinceVersion: Int = 0
) : ComponentTransformer<T>(typeId, sinceVersion) {

    override fun getLegacy(nmsItem: Any): T? {
        val item = NMSItemTag.asBukkitCopy(nmsItem)
        return item.getItemTag().getDeep(nbtPath)?.let { translator.fromTag(it) }
    }

    override fun setLegacy(nmsItem: Any, value: T) {
        val item = NMSItemTag.asBukkitCopy(nmsItem)
        val tag = item.getItemTag()
        tag.putDeep(nbtPath, translator.toTag(value))
        copyBackToNms(tag.saveTo(item), nmsItem)
    }

    override fun removeLegacy(nmsItem: Any) {
        val item = NMSItemTag.asBukkitCopy(nmsItem)
        val tag = item.getItemTag()
        tag.removeDeep(nbtPath)
        copyBackToNms(tag.saveTo(item), nmsItem)
    }

    override fun hasLegacy(nmsItem: Any): Boolean {
        val item = NMSItemTag.asBukkitCopy(nmsItem)
        return item.getItemTag().getDeep(nbtPath) != null
    }

    private fun copyBackToNms(bukkit: ItemStack, nmsItem: Any) {
        NMSItemTagHelper.copyNmsItemData(NMSItemTag.asNMSCopy(bukkit), nmsItem)
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

### 3.3 ComposedType\<T\> — 组件描述符（纯数据）

```kotlin
/**
 * 对标 NMS DataComponentType<T>
 * 纯描述符，不持有 Transformer 实例
 * Transformer 通过 TransformerRegistry 独立管理
 */
class ComposedType<T>(
    val id: String,
    val translator: ComponentTranslator<T>,
    val sinceVersion: Int = 0
) {
    /** 序列化 */
    fun serialize(value: T): ItemTagData = translator.toTag(value)

    /** 反序列化 */
    fun deserialize(data: ItemTagData): T = translator.fromTag(data)

    override fun equals(other: Any?) = other is ComposedType<*> && other.id == id
    override fun hashCode() = id.hashCode()
    override fun toString() = "ComposedType($id)"
}
```

### 3.4 TransformerRegistry — Transformer 独立管理

```kotlin
/**
 * Transformer 注册表，独立于 ComposedType
 * ComposedType 只描述"是什么"，TransformerRegistry 管理"怎么做"
 */
object TransformerRegistry {

    private val registry = ConcurrentHashMap<String, ComponentTransformer<*>>()

    fun <T> register(type: ComposedType<T>, transformer: ComponentTransformer<T>) {
        registry[type.id] = transformer
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(type: ComposedType<T>): ComponentTransformer<T>? {
        return registry[type.id] as? ComponentTransformer<T>
    }

    fun has(type: ComposedType<*>): Boolean = registry.containsKey(type.id)

    fun all(): Map<String, ComponentTransformer<*>> = registry.toMap()
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
        return TransformerRegistry.get(type)?.get(thisRef.nmsItem) ?: default
    }

    override fun setValue(thisRef: ComposedItem, property: KProperty<*>, value: T) {
        TransformerRegistry.get(type)?.set(thisRef.nmsItem, value)
    }
}

/**
 * 可空属性委托（无默认值）
 */
class NullableComponentDelegate<T>(
    val type: ComposedType<T>
) : ReadWriteProperty<ComposedItem, T?> {

    override fun getValue(thisRef: ComposedItem, property: KProperty<*>): T? {
        return TransformerRegistry.get(type)?.get(thisRef.nmsItem)
    }

    override fun setValue(thisRef: ComposedItem, property: KProperty<*>, value: T?) {
        TransformerRegistry.get(type)?.setOrRemove(thisRef.nmsItem, value)
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
class ComposedItem(internal val nmsItem: Any /* 克隆的 NMS ItemStack */) {

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

    // ══════════ 泛型方法 ══════════
    fun <T> get(type: ComposedType<T>): T? = TransformerRegistry.get(type)?.get(nmsItem)
    fun <T> set(type: ComposedType<T>, value: T) { TransformerRegistry.get(type)?.set(nmsItem, value) }
    fun <T> remove(type: ComposedType<T>) { TransformerRegistry.get(type)?.remove(nmsItem) }
    fun <T> has(type: ComposedType<T>): Boolean = TransformerRegistry.get(type)?.has(nmsItem) ?: false

    // ══════════ 操作符重载 ══════════

    /** composed[type] — 等价于 get(type) */
    operator fun <T> get(type: ComposedType<T>): T? = get(type)

    /** composed[type] = value — 等价于 set(type, value) */
    operator fun <T> set(type: ComposedType<T>, value: T) = set(type, value)

    /** type in composed — 等价于 has(type) */
    operator fun contains(type: ComposedType<*>): Boolean = has(type)

    /** composed -= type — 等价于 remove(type) */
    operator fun minusAssign(type: ComposedType<*>) {
        @Suppress("UNCHECKED_CAST")
        remove(type as ComposedType<Any>)
    }

    // ══════════ 保存 ══════════

    /** 将克隆体的修改写回原物品 */
    fun saveTo(item: ItemStack) {
        val newItem = NMSItemTag.asBukkitCopy(nmsItem)
        item.type = newItem.type
        item.amount = newItem.amount
        item.itemMeta = newItem.itemMeta
    }

    /** 从克隆体生成新物品 */
    fun toItemStack(): ItemStack {
        return NMSItemTag.asBukkitCopy(nmsItem)
    }
}
```

### 4.2 入口扩展函数

```kotlin
/** 克隆物品的 NMS 副本，返回 ComposedItem */
fun ItemStack.getComposedItem(): ComposedItem {
    val nmsClone = NMSItemTag.asNMSCopy(this)  // 克隆发生在这里
    return ComposedItem(nmsClone)
}
```

### 4.3 使用示例

```kotlin
// 基本读写（属性委托）
val composed = item.getComposedItem()
composed.customName = "§6Excalibur"
composed.lore = listOf("§7A legendary sword", "§5Damage: +100")
composed.isUnbreakable = true
composed.damage = 0

// 操作符访问
composed[ComposedTypes.DAMAGE] = 100
val dmg = composed[ComposedTypes.DAMAGE]
if (ComposedTypes.FOOD in composed) { /* ... */ }
composed -= ComposedTypes.ENCHANTMENT_GLINT_OVERRIDE

// 复合类型修改（copy-modify-set）
composed.food = FoodComponent(nutrition = 5, saturation = 0.6f)
composed.food = composed.food?.copy(canAlwaysEat = true)

// 集合操作
composed.enchantments = composed.enchantments + ("minecraft:sharpness" to 5)
composed.lore = composed.lore + "§7New line"

// 保存
composed.saveTo(item)                  // 写回原物品
val newItem = composed.toItemStack()   // 生成新物品

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

    // ══════════ NMS 组件（高版本扩展预留） ══════════
    fun <T> get(type: ComposedType<T>): T? =
        TransformerRegistry.get(type)?.get(getNmsEntity())

    fun <T> set(type: ComposedType<T>, value: T) {
        TransformerRegistry.get(type)?.set(getNmsEntity(), value)
    }

    private fun getNmsEntity(): Any {
        return entity::class.java.getMethod("getHandle").invoke(entity)
    }
}

fun Entity.getComposedEntity(): ComposedEntity = ComposedEntity(this)
```

### 5.2 与 ComposedItem 的区别

| | ComposedItem | ComposedEntity |
|---|---|---|
| 内部对象 | 克隆的 NMS ItemStack | Bukkit Entity 引用 |
| 修改方式 | 修改克隆体，手动保存 | 直接生效（Bukkit API 原地修改） |
| saveTo/toItemStack | 需要 | 不需要 |
| 版本适配 | Transformer 内部分支 | 大部分走 Bukkit API，少量走 NMS |

## 6. 版本适配策略

### 6.1 Transformer 示例 — CustomNameTransformer

```kotlin
class CustomNameTransformer : NBTPathTransformer<String>(
    typeId = "minecraft:custom_name",
    nbtPath = "display.Name",
    translator = JsonComponentTranslator
) {
    // Modern 部分通过 nmsProxy/dynamic 操作 DataComponents
    private val accessor by unsafeLazy {
        nmsProxy<DataComponentAccessor>("...CustomNameAccessor")
    }

    override fun getModern(nmsItem: Any): String? {
        return accessor.get(nmsItem)
    }

    override fun setModern(nmsItem: Any, value: String) {
        accessor.set(nmsItem, value)
    }

    override fun removeModern(nmsItem: Any) {
        accessor.remove(nmsItem)
    }

    override fun hasModern(nmsItem: Any): Boolean {
        return accessor.has(nmsItem)
    }

    // Legacy 部分由 NBTPathTransformer 基类自动处理（display.Name 路径）
}
```

### 6.2 版本内部差异处理

Modern 实现中，不同子版本的 NMS 方法签名可能不同，通过 `dynamic` 处理：

```kotlin
// 1.20.5 vs 1.21.5 方法签名不同时
val value = if (MinecraftVersion.versionId >= 12105) {
    dynamic(DynamicOpcode.INVOKEVIRTUAL, "...#value()...", nmsObj)
} else {
    dynamic(DynamicOpcode.INVOKEVIRTUAL, "...#getAsInt()...", nmsObj)
}
```

### 6.3 版本降级对照表

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
    val blocks: List<String>,
    val speed: Float? = null,
    val correctForDrops: Boolean? = null
)

data class AttributeModifiersComponent(
    val modifiers: List<AttributeEntry> = emptyList(),
    val showInTooltip: Boolean = true
)

data class AttributeEntry(
    val attribute: String,
    val name: String,
    val amount: Double,
    val operation: AttributeOperation,
    val slot: EquipmentSlotGroup = EquipmentSlotGroup.ANY
)

data class ArmorTrimComponent(
    val material: String,
    val pattern: String,
    val showInTooltip: Boolean = true
)

data class DyedColorComponent(
    val color: Int,
    val showInTooltip: Boolean = true
)

data class PotionContentsComponent(
    val potion: String? = null,
    val customColor: Int? = null,
    val customEffects: List<PotionEffect> = emptyList()
)

data class PotionEffect(
    val id: String,
    val amplifier: Int = 0,
    val duration: Int = 200,
    val ambient: Boolean = false,
    val showParticles: Boolean = true,
    val showIcon: Boolean = true
)

data class WritableBookComponent(val pages: List<String> = emptyList())

data class WrittenBookComponent(
    val title: String = "",
    val author: String = "",
    val generation: Int = 0,
    val pages: List<String> = emptyList(),
    val resolved: Boolean = false
)

data class FireworksComponent(
    val flightDuration: Int = 0,
    val explosions: List<FireworkExplosion> = emptyList()
)

data class FireworkExplosion(
    val shape: String = "small_ball",
    val colors: List<Int> = emptyList(),
    val fadeColors: List<Int> = emptyList(),
    val hasTrail: Boolean = false,
    val hasTwinkle: Boolean = false
)

data class ProfileComponent(
    val name: String? = null,
    val uuid: UUID? = null,
    val properties: Map<String, String> = emptyMap()
)

data class BannerPatternEntry(val pattern: String, val color: String)

data class LodestoneTrackerComponent(
    val target: LodestoneTarget? = null,
    val tracked: Boolean = true
)

data class LodestoneTarget(
    val dimension: String,
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
    TransformerRegistry.all().forEach { (id, transformer) ->
        @Suppress("UNCHECKED_CAST")
        val t = transformer as ComponentTransformer<Any>
        val value = t.get(nmsItem) ?: return@forEach
        val type = ComposedRegistry.get(id) ?: return@forEach
        @Suppress("UNCHECKED_CAST")
        tag[id] = (type as ComposedType<Any>).serialize(value)
    }
    return tag
}

/** 从快照恢复 */
fun ComposedItem.restore(snapshot: ItemTag) {
    snapshot.forEach { (id, data) ->
        @Suppress("UNCHECKED_CAST")
        val type = ComposedRegistry.get(id) as? ComposedType<Any> ?: return@forEach
        val transformer = TransformerRegistry.get(type) ?: return@forEach
        val value = type.deserialize(data)
        transformer.set(nmsItem, value)
    }
}
```

## 9. 自定义扩展机制

### 9.1 ComposedRegistry（类型注册表）

```kotlin
/**
 * ComposedType 注册表
 * 与 TransformerRegistry 配合：ComposedRegistry 管理"是什么"，TransformerRegistry 管理"怎么做"
 */
object ComposedRegistry {

    private val registry = ConcurrentHashMap<String, ComposedType<*>>()

    fun register(type: ComposedType<*>) { registry[type.id] = type }
    fun unregister(id: String) { registry.remove(id) }
    fun get(id: String): ComposedType<*>? = registry[id]
    fun all(): Collection<ComposedType<*>> = registry.values
    fun has(id: String): Boolean = registry.containsKey(id)
}
```

### 9.2 插件自定义组件示例

```kotlin
// 定义类型 + Transformer
object MyPlugin {
    val SOUL_BOUND = ComposedType<Boolean>(
        id = "myplugin:soul_bound",
        translator = BooleanTranslator
    )
}

class SoulBoundTransformer : NBTPathTransformer<Boolean>(
    typeId = "myplugin:soul_bound",
    nbtPath = "MyPlugin.SoulBound",
    translator = BooleanTranslator
) {
    override fun getModern(nmsItem: Any): Boolean? = getLegacy(nmsItem) // 复用 NBT
    override fun setModern(nmsItem: Any, value: Boolean) = setLegacy(nmsItem, value)
    override fun removeModern(nmsItem: Any) = removeLegacy(nmsItem)
    override fun hasModern(nmsItem: Any): Boolean = hasLegacy(nmsItem)
}

// 注册（在插件 onEnable 中）
ComposedRegistry.register(MyPlugin.SOUL_BOUND)
TransformerRegistry.register(MyPlugin.SOUL_BOUND, SoulBoundTransformer())

// 使用
val composed = item.getComposedItem()
val isSoulBound = composed.get(MyPlugin.SOUL_BOUND) ?: false
composed.set(MyPlugin.SOUL_BOUND, true)
composed.saveTo(item)
```

## 10. ComposedTypes 静态字段 + Transformer 注册

类型定义与 Transformer 注册分离。`ComposedTypes` 只定义描述符，`ComposedTransformers` 负责注册。

### 10.1 类型描述符

```kotlin
object ComposedTypes {

    // ══════════ 基础 ══════════
    val CUSTOM_NAME = ComposedType("minecraft:custom_name", JsonComponentTranslator)
    val ITEM_NAME = ComposedType("minecraft:item_name", JsonComponentTranslator, sinceVersion = 12005)
    val LORE = ComposedType("minecraft:lore", JsonComponentListTranslator)
    val DAMAGE = ComposedType("minecraft:damage", IntTranslator)
    val MAX_DAMAGE = ComposedType("minecraft:max_damage", IntTranslator, sinceVersion = 12005)
    val MAX_STACK_SIZE = ComposedType("minecraft:max_stack_size", IntTranslator, sinceVersion = 12005)
    val REPAIR_COST = ComposedType("minecraft:repair_cost", IntTranslator)
    val CUSTOM_MODEL_DATA = ComposedType("minecraft:custom_model_data", IntTranslator)
    val RARITY = ComposedType("minecraft:rarity", RarityTranslator, sinceVersion = 12005)

    // ══════════ 布尔/标记 ══════════
    val UNBREAKABLE = ComposedType("minecraft:unbreakable", BooleanTranslator)
    val HIDE_TOOLTIP = ComposedType("minecraft:hide_tooltip", BooleanTranslator, sinceVersion = 12005)
    val HIDE_ADDITIONAL_TOOLTIP = ComposedType("minecraft:hide_additional_tooltip", BooleanTranslator, sinceVersion = 12005)
    val ENCHANTMENT_GLINT_OVERRIDE = ComposedType("minecraft:enchantment_glint_override", BooleanTranslator, sinceVersion = 12005)
    val FIRE_RESISTANT = ComposedType("minecraft:fire_resistant", BooleanTranslator, sinceVersion = 12005)

    // ══════════ 集合 ══════════
    val ENCHANTMENTS = ComposedType("minecraft:enchantments", EnchantmentMapTranslator)
    val STORED_ENCHANTMENTS = ComposedType("minecraft:stored_enchantments", EnchantmentMapTranslator)
    val CAN_BREAK = ComposedType("minecraft:can_break", StringListTranslator)
    val CAN_PLACE_ON = ComposedType("minecraft:can_place_on", StringListTranslator)

    // ══════════ 复合 ══════════
    val FOOD = ComposedType("minecraft:food", FoodTranslator, sinceVersion = 12005)
    val CONSUMABLE = ComposedType("minecraft:consumable", ConsumableTranslator, sinceVersion = 12102)
    val TOOL = ComposedType("minecraft:tool", ToolTranslator, sinceVersion = 12005)
    val ATTRIBUTE_MODIFIERS = ComposedType("minecraft:attribute_modifiers", AttributeModifiersTranslator)
    val TRIM = ComposedType("minecraft:trim", ArmorTrimTranslator, sinceVersion = 11904)
    val DYED_COLOR = ComposedType("minecraft:dyed_color", DyedColorTranslator)
    val CUSTOM_DATA = ComposedType("minecraft:custom_data", ItemTagTranslator)

    // ══════════ 容器/特殊 ══════════
    val POTION_CONTENTS = ComposedType("minecraft:potion_contents", PotionContentsTranslator)
    val WRITABLE_BOOK_CONTENT = ComposedType("minecraft:writable_book_content", WritableBookTranslator)
    val WRITTEN_BOOK_CONTENT = ComposedType("minecraft:written_book_content", WrittenBookTranslator)
    val FIREWORKS = ComposedType("minecraft:fireworks", FireworksTranslator)
    val PROFILE = ComposedType("minecraft:profile", ProfileTranslator)
    val BANNER_PATTERNS = ComposedType("minecraft:banner_patterns", BannerPatternsTranslator)
    val LODESTONE_TRACKER = ComposedType("minecraft:lodestone_tracker", LodestoneTrackerTranslator)
    val BUNDLE_CONTENTS = ComposedType("minecraft:bundle_contents", ItemStackListTranslator, sinceVersion = 11700)
    val CONTAINER = ComposedType("minecraft:container", ItemStackListTranslator, sinceVersion = 12005)
}
```

### 10.2 Transformer 注册（框架启动时）

```kotlin
/**
 * 在模块初始化阶段（@Awake CONST）注册所有内置 Transformer
 */
object ComposedTransformers {

    @Awake(LifeCycle.CONST)
    fun init() {
        // 基础
        reg(ComposedTypes.CUSTOM_NAME, CustomNameTransformer())
        reg(ComposedTypes.ITEM_NAME, ItemNameTransformer())
        reg(ComposedTypes.LORE, LoreTransformer())
        reg(ComposedTypes.DAMAGE, DamageTransformer())
        reg(ComposedTypes.MAX_DAMAGE, MaxDamageTransformer())
        reg(ComposedTypes.REPAIR_COST, RepairCostTransformer())
        reg(ComposedTypes.CUSTOM_MODEL_DATA, CustomModelDataTransformer())
        reg(ComposedTypes.UNBREAKABLE, UnbreakableTransformer())
        reg(ComposedTypes.ENCHANTMENTS, EnchantmentsTransformer())
        // ... 其余组件
    }

    private fun <T> reg(type: ComposedType<T>, transformer: ComponentTransformer<T>) {
        ComposedRegistry.register(type)
        TransformerRegistry.register(type, transformer)
    }
}
```

## 11. 模块结构

```
module/bukkit-nms/bukkit-nms-data-component/
│
├── DESIGN.md
├── build.gradle.kts
│
└── src/main/kotlin/taboolib/module/nms/
    │
    │  ── 核心框架 ──
    ├── ComponentTransformer.kt
    ├── NBTPathTransformer.kt
    ├── ComponentTranslator.kt
    ├── ComposedType.kt
    ├── ComposedItem.kt
    ├── ComposedEntity.kt
    ├── ComposedRegistry.kt
    ├── TransformerRegistry.kt
    ├── ComponentDelegate.kt
    │
    │  ── 数据类型 ──
    ├── component/
    │   ├── ItemRarity.kt
    │   ├── FoodComponent.kt
    │   ├── ToolComponent.kt
    │   ├── AttributeModifiersComponent.kt
    │   ├── ... (其余 data class)
    │   └── LodestoneTrackerComponent.kt
    │
    │  ── 翻译器 ──
    ├── translator/
    │   ├── IntTranslator.kt
    │   ├── BooleanTranslator.kt
    │   ├── StringTranslator.kt
    │   ├── JsonComponentTranslator.kt
    │   └── ... (各复合类型翻译器)
    │
    │  ── Transformer 实现 ──
    ├── transformer/
    │   ├── CustomNameTransformer.kt
    │   ├── LoreTransformer.kt
    │   ├── DamageTransformer.kt
    │   ├── EnchantmentsTransformer.kt
    │   ├── UnbreakableTransformer.kt
    │   ├── FoodTransformer.kt
    │   └── ... (每个组件一个 Transformer)
    │
    │  ── 静态字段 + 注册 ──
    ├── ComposedTypes.kt
    └── ComposedTransformers.kt
```

## 12. 实现计划

### Phase 1：核心框架

- [ ] `ComponentTransformer` 抽象类 + `NBTPathTransformer` 便捷基类
- [ ] `ComponentTranslator` 接口 + 基础翻译器
- [ ] `ComposedType` 纯描述符
- [ ] `TransformerRegistry` + `ComposedRegistry`
- [ ] `ComponentDelegate` + `NullableComponentDelegate`
- [ ] `ComposedItem` 骨架（克隆、saveTo、toItemStack、泛型方法、操作符）
- [ ] `ComposedEntity` 骨架
- [ ] `build.gradle.kts` 模块配置

### Phase 2：基础组件 Transformer

- [ ] CustomNameTransformer (Modern + Legacy)
- [ ] LoreTransformer (Modern + Legacy)
- [ ] DamageTransformer (Modern + Legacy)
- [ ] EnchantmentsTransformer (Modern + Legacy，含 1.13 前兼容)
- [ ] UnbreakableTransformer (Modern + Legacy)
- [ ] CustomDataTransformer (Modern + Legacy)
- [ ] CustomModelDataTransformer (Modern + Legacy)
- [ ] RepairCostTransformer (Modern + Legacy)
- [ ] CanBreak/CanPlaceOnTransformer (Modern + Legacy)

### Phase 3：复合组件 Transformer

- [ ] FoodTransformer / ConsumableTransformer
- [ ] ToolTransformer
- [ ] AttributeModifiersTransformer
- [ ] PotionContentsTransformer
- [ ] DyedColorTransformer
- [ ] ArmorTrimTransformer

### Phase 4：特殊组件 Transformer

- [ ] ProfileTransformer (头颅)
- [ ] FireworksTransformer (烟花)
- [ ] BannerPatternsTransformer (旗帜)
- [ ] WrittenBook/WritableBookTransformer (书本)
- [ ] LodestoneTrackerTransformer (磁石)
- [ ] BundleContents/ContainerTransformer (容器)
- [ ] MaxStackSize/MaxDamage/Rarity/FireResistant 等标记组件

### Phase 5：完善

- [ ] 序列化快照 snapshot / restore
- [ ] ComposedTransformers 注册入口
- [ ] 文档和使用示例
- [ ] 单元测试

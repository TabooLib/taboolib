# 状态匹配器 StateMatcher

状态匹配器（`IStateMatcher`）定义了"某个位置应该是什么方块"的规则。是多方块结构的核心构建块。

## IStateMatcher 接口

```kotlin
interface IStateMatcher {
    val displayName: String       // 显示名称
    fun test(block: Block): Boolean  // 测试方块是否匹配
}
```

## 内置常量

| 常量 | 说明 | 对应 Dense 特殊字符 |
|------|------|-------------------|
| `StateMatcher.ANY` | 匹配任意方块（包括空气） | `_` |
| `StateMatcher.AIR` | 仅匹配空气方块 | ` ` (空格) 和 `0` |

## StateMatcher 工厂方法

### fromMaterial — 匹配材质

匹配指定材质，忽略方块状态属性（如 `facing`、`half` 等）：

```kotlin
StateMatcher.fromMaterial(Material.STONE)
StateMatcher.fromMaterial(Material.FURNACE)
StateMatcher.fromMaterial(Material.OAK_PLANKS)
```

### fromBlockData — 精确匹配方块数据

包括状态属性的精确匹配：

```kotlin
val data = Bukkit.createBlockData("minecraft:oak_stairs[facing=north,half=bottom]")
StateMatcher.fromBlockData(data)
```

### fromBlockDataLoose — 宽松匹配方块数据

使用 `BlockData.matches()` 进行宽松匹配（仅检查指定的属性）：

```kotlin
StateMatcher.fromBlockDataLoose(blockData)
```

### fromPredicate — 自定义谓词

完全自定义的匹配逻辑：

```kotlin
// 匹配铁块或金块
StateMatcher.fromPredicate("iron_or_gold") { block ->
    block.type == Material.IRON_BLOCK || block.type == Material.GOLD_BLOCK
}

// 匹配所有矿石
StateMatcher.fromPredicate("any_ore") { block ->
    block.type.name.endsWith("_ORE")
}

// 匹配亮度足够的方块
StateMatcher.fromPredicate("bright") { block ->
    block.lightLevel >= 12
}
```

### displayOnly — 仅显示

不进行实际验证（始终返回 true），仅用于 UI 显示目的：

```kotlin
StateMatcher.displayOnly("decorative")
```

## StringStateMatcher — 字符串解析

`StringStateMatcher.parse()` 从字符串解析匹配器，支持三种格式：

### 材质名匹配

匹配指定材质，忽略方块状态属性：

```kotlin
StringStateMatcher.parse("minecraft:stone")
StringStateMatcher.parse("minecraft:oak_planks")
StringStateMatcher.parse("minecraft:furnace")
StringStateMatcher.parse("stone")  // 也支持省略命名空间
```

### 方块数据匹配（带属性）

方括号内指定需要匹配的状态属性：

```kotlin
StringStateMatcher.parse("minecraft:oak_stairs[facing=north]")
StringStateMatcher.parse("minecraft:oak_stairs[facing=north,half=bottom]")
StringStateMatcher.parse("minecraft:chest[facing=south,type=single]")
StringStateMatcher.parse("minecraft:note_block[note=4]")
StringStateMatcher.parse("minecraft:brick_slab[type=bottom,waterlogged=true]")
```

### 标签匹配

以 `#` 开头，匹配标签中的所有方块：

```kotlin
StringStateMatcher.parse("#minecraft:planks")      // 所有木板
StringStateMatcher.parse("#minecraft:wool")         // 所有羊毛
StringStateMatcher.parse("#minecraft:logs")         // 所有原木
StringStateMatcher.parse("#minecraft:buttons")      // 所有按钮
StringStateMatcher.parse("#minecraft:stone_bricks") // 所有石砖变种
```

### 流体方块

流体同样可以作为匹配条件：

```kotlin
StringStateMatcher.parse("minecraft:water")
StringStateMatcher.parse("minecraft:lava")
```

## 自定义匹配器

实现 `IStateMatcher` 接口：

```kotlin
class LightLevelMatcher(private val minLight: Int) : IStateMatcher {
    override val displayName = "light>=$minLight"
    override fun test(block: Block): Boolean {
        return block.lightLevel >= minLight
    }
}

// 使用
val mapping = mapOf(
    'L' to LightLevelMatcher(12)
)
```

## 在 DenseMultiblock 中使用

```kotlin
val multiblock = DenseMultiblock(
    pattern = arrayOf(
        arrayOf(
            "FIF",
            "I0I",
            "FIF"
        )
    ),
    mapping = mapOf(
        'F' to StateMatcher.fromMaterial(Material.FURNACE),
        'I' to StateMatcher.fromPredicate("iron_or_gold") { block ->
            block.type == Material.IRON_BLOCK || block.type == Material.GOLD_BLOCK
        },
        '0' to StringStateMatcher.parse("minecraft:lapis_block"),
    )
)
```

## 注意事项

| 场景 | 说明 |
|------|------|
| 材质不存在 | `StringStateMatcher.MaterialMatcher` 返回 `false` |
| BlockData 解析失败 | `StringStateMatcher.BlockDataMatcher` 返回 `false`（不抛异常） |
| 标签不存在 | `StringStateMatcher.TagMatcher` 返回 `false` |
| `ANY` 匹配器 | 在 `DenseMultiblock.simulate()` 中被跳过，不生成 `SimulateResult` |
| mapping 优先级 | 用户定义的 mapping 会覆盖默认的特殊字符映射 |

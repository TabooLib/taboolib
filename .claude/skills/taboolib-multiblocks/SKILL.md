---
name: taboolib-multiblocks
description: 在 TabooLib 中，定义和验证多方块结构（Multiblock）。 (project)
---

# TabooLib-Multiblocks 多方块结构工具

## 编写规则

如果要使用多方块结构功能，要遵守以下规则：

1. **选择合适的结构类型**：紧凑结构用 `DenseMultiblock`（字符图案），大型稀疏结构用 `SparseMultiblock`（坐标映射）
2. **密集型图案方向**：`pattern` 数组第一个元素 = 最顶层（最高 Y），最后一个 = 最底层；每个 `String` 数组元素是一行 Z（北→南），字符串中每个字符是 X 列（西→东）
3. **必须设置锚点**：密集型中 `'0'` 字符**必须恰好出现一次**作为锚点；稀疏型锚点默认在 `(0,0,0)`
4. **映射必须完整**：密集型图案中出现的所有字符（除 `'0'`、`'_'`、空格外）都必须在 `mapping` 中定义，否则会映射为空气
5. **使用 `StringStateMatcher.parse()` 解析方块**：支持 `minecraft:stone`（材质）、`minecraft:stairs[facing=north]`（带属性）、`#minecraft:wool`（标签）三种格式
6. **对称结构设置 `symmetrical = true`**：如果结构在水平面上关于中心轴对称，设置此标志可跳过多余旋转检测，性能提升约 4 倍
7. **通过 `MultiblockRegistry` 注册**：使用 `命名空间:名称` 格式的 ID 进行全局注册

## 类型表

| 类 | 说明 |
|---|---|
| `DenseMultiblock` | 密集型：用字符图案 `Array<Array<String>>` 定义结构 |
| `SparseMultiblock` | 稀疏型：用 `Map<BlockPos, IStateMatcher>` 定义结构 |
| `StateMatcher` | 内置匹配器工厂（`ANY`/`AIR`/`fromMaterial`/`fromBlockData`/`fromPredicate`/`displayOnly`） |
| `StringStateMatcher` | 从字符串解析匹配器（材质名/方块属性/标签） |
| `MultiblockRegistry` | 全局注册表（`register`/`get`/`unregister`/`getAll`/`clear`） |
| `BlockPos` | 整数坐标，支持 `+`/`-` 运算和 `rotate()` 旋转 |
| `MultiblockRotation` | 旋转枚举（`NONE`/`CLOCKWISE_90`/`CLOCKWISE_180`/`COUNTERCLOCKWISE_90`） |
| `IMultiblock` | 核心接口（`validate`/`simulate`/`test`/`offset`） |
| `IStateMatcher` | 方块匹配器接口（`displayName`/`test`） |
| `SimulateResult` | 模拟结果（`worldPosition`/`stateMatcher`/`character`） |

## 特殊字符（DenseMultiblock）

| 字符 | 含义 | 默认匹配器 | 可否在 mapping 中覆盖 |
|------|------|-----------|---------------------|
| `0` | 中心锚点（必须恰好一个） | `StateMatcher.AIR` | 可覆盖（如 `'0' to parse("minecraft:lapis_block")`） |
| `_` | 任意方块 | `StateMatcher.ANY` | 可覆盖 |
| ` ` (空格) | 空气 | `StateMatcher.AIR` | 可覆盖（如 `' ' to StateMatcher.ANY`） |

## 字符串匹配器格式

| 格式 | 示例 | 说明 |
|------|------|------|
| 材质名 | `minecraft:stone` | 匹配材质，忽略方块状态属性 |
| 方块数据 | `minecraft:oak_stairs[facing=north,half=bottom]` | 精确匹配，包括指定的状态属性 |
| 方块标签 | `#minecraft:wool` | 匹配标签中的所有方块 |

## 旋转变换

| 枚举值 | 角度 | `(x, y, z)` → |
|--------|------|----------------|
| `NONE` | 0° | `(x, y, z)` |
| `CLOCKWISE_90` | 顺时针 90° | `(-z, y, x)` |
| `CLOCKWISE_180` | 180° | `(-x, y, -z)` |
| `COUNTERCLOCKWISE_90` | 逆时针 90° | `(z, y, -x)` |

## 快速示例

### 密集型多方块

```kotlin
val altar = DenseMultiblock(
    pattern = arrayOf(
        // 顶层
        arrayOf(
            "   ",
            " S ",
            "   "
        ),
        // 底层
        arrayOf(
            "SSS",
            "S0S",
            "SSS"
        )
    ),
    mapping = mapOf(
        'S' to StringStateMatcher.parse("minecraft:stone_bricks")
    )
)
MultiblockRegistry.register("my_plugin:altar", altar)
```

### 带朝向属性和自定义锚点方块

```kotlin
val stairAltar = DenseMultiblock(
    pattern = arrayOf(
        arrayOf(
            " WWW ",
            "N   S",
            "N 0 S",
            "N   S",
            " EEE "
        )
    ),
    mapping = mapOf(
        ' ' to StateMatcher.ANY,
        '0' to StringStateMatcher.parse("minecraft:lapis_block"),
        'N' to StringStateMatcher.parse("minecraft:oak_stairs[facing=south]"),
        'S' to StringStateMatcher.parse("minecraft:oak_stairs[facing=north]"),
        'W' to StringStateMatcher.parse("minecraft:oak_stairs[facing=east]"),
        'E' to StringStateMatcher.parse("minecraft:oak_stairs[facing=west]"),
    )
).apply { symmetrical = true }
```

### 包含流体和含水方块

```kotlin
val fluidStructure = DenseMultiblock(
    pattern = arrayOf(
        arrayOf(
            "GGGGGGG",
            " LLLLLG",
            "GGGGGGG",
            " WW0WWG",
            "GGGGGGG",
        )
    ),
    mapping = mapOf(
        ' ' to StateMatcher.ANY,
        '0' to StringStateMatcher.parse("minecraft:lapis_block"),
        'G' to StringStateMatcher.parse("minecraft:bricks"),
        'W' to StringStateMatcher.parse("minecraft:water"),
        'L' to StringStateMatcher.parse("minecraft:lava"),
    )
)
```

### 稀疏型多方块

```kotlin
val fourPillars = SparseMultiblock(
    blocks = buildMap {
        val pillar = StringStateMatcher.parse("minecraft:stone_bricks")
        for (x in listOf(-3, 3)) {
            for (z in listOf(-3, 3)) {
                for (y in 0..2) {
                    put(BlockPos(x, y, z), pillar)
                }
            }
        }
        put(BlockPos(0, 0, 0), StringStateMatcher.parse("minecraft:diamond_block"))
    }
)
```

### 验证结构

```kotlin
val anchor = BlockPos(block.x, block.y, block.z)
// 自动尝试所有旋转（非对称 = 4 方向，对称 = 1 方向）
val rotation = multiblock.validate(world, anchor)
if (rotation != null) {
    // 匹配成功，rotation 为匹配时的旋转方向
}
```

### 进度检测

```kotlin
val results = multiblock.simulate(anchor, rotation)
for (result in results) {
    val pos = result.worldPosition
    val block = world.getBlockAt(pos.x, pos.y, pos.z)
    if (!result.stateMatcher.test(block)) {
        println("缺失: $pos 需要 ${result.stateMatcher.displayName}")
    }
}
```

### 自定义匹配器

```kotlin
val mapping = mapOf(
    'F' to StateMatcher.fromMaterial(Material.FURNACE),
    'I' to StateMatcher.fromPredicate("iron_or_gold") { block ->
        block.type == Material.IRON_BLOCK || block.type == Material.GOLD_BLOCK
    },
    'W' to StringStateMatcher.parse("#minecraft:planks"),
    'T' to StringStateMatcher.parse("#minecraft:logs"),
)
```

## 参考文档

- [dense-multiblock.md](dense-multiblock.md) - 密集型多方块：图案格式、坐标系、特殊字符
- [sparse-multiblock.md](sparse-multiblock.md) - 稀疏型多方块：坐标映射、与密集型对比
- [state-matcher.md](state-matcher.md) - 状态匹配器：内置匹配器、字符串解析、自定义匹配器
- [validation.md](validation.md) - 结构验证：旋转检测、对称优化、模拟、进度检测

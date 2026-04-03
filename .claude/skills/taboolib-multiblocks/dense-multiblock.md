# 密集型多方块 DenseMultiblock

## 构造函数

```kotlin
class DenseMultiblock(
    pattern: Array<Array<String>>,
    mapping: Map<Char, IStateMatcher> = emptyMap()
) : AbstractMultiblock()
```

**参数说明：**

- `pattern`：结构图案，三层嵌套数组
- `mapping`：字符到 `IStateMatcher` 的映射

## 图案坐标系

Pattern 是一个三维结构，索引含义如下：

```
pattern = arrayOf(           ← 外层数组：Y 层（index 0 = 最高层）
    arrayOf(                 ←   中层数组：Z 行（index 0 = 北，从北→南）
        "ABCDE",             ←     字符串字符：X 列（index 0 = 西，从西→东）
        "FGHIJ",
        "KLMNO"
    )
)
```

| 维度 | 索引方向 | 说明 |
|------|---------|------|
| **Y 层** | `pattern[0]` = 最顶层，`pattern[N]` = 最底层 | 从上到下排列 |
| **Z 行** | `layer[0]` = Z=0（北），`layer[N]` = 最大 Z（南） | 从北到南排列 |
| **X 列** | `row[0]` = X=0（西），`row[N]` = 最大 X（东） | 从西到东排列 |

**俯视图（单层）：**

```
         X（西→东）
         0  1  2
Z  0  [  A  B  C  ]    ← 北
（  1  [  D  E  F  ]
北  2  [  G  H  I  ]    ← 南
→
南）
```

## 特殊字符

| 字符 | 含义 | 默认匹配器 | 可覆盖 |
|------|------|-----------|--------|
| `0` | 中心锚点（**必须恰好一个**） | `StateMatcher.AIR` | 可在 mapping 中覆盖 |
| `_` | 任意方块 | `StateMatcher.ANY` | 可覆盖 |
| ` ` (空格) | 空气 | `StateMatcher.AIR` | 可覆盖 |

**重要规则：**
- 图案中**必须恰好包含一个 `0`** 字符作为锚点
- 图案中出现的所有其他字符（除 `0`、`_`、空格外）**必须在 mapping 中定义**
- 特殊字符可在 mapping 中覆盖默认行为

## 基础示例

### 单层 3x3 平台

```kotlin
val platform = DenseMultiblock(
    pattern = arrayOf(
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
```

### 多层结构

```kotlin
// 3x3x3 空心立方体
val cube = DenseMultiblock(
    pattern = arrayOf(
        // 顶层 (最高 Y)
        arrayOf(
            "BBB",
            "BBB",
            "BBB"
        ),
        // 中间层，含锚点
        arrayOf(
            "BBB",
            "B0B",
            "BBB"
        ),
        // 底层 (最低 Y)
        arrayOf(
            "BBB",
            "BBB",
            "BBB"
        )
    ),
    mapping = mapOf(
        'B' to StringStateMatcher.parse("minecraft:bricks")
    )
)
```

### 自定义锚点方块

默认情况下 `'0'` 映射为空气。如果锚点位置需要特定方块，在 mapping 中覆盖：

```kotlin
val altar = DenseMultiblock(
    pattern = arrayOf(
        arrayOf(
            "SSS",
            "S0S",
            "SSS"
        )
    ),
    mapping = mapOf(
        '0' to StringStateMatcher.parse("minecraft:lapis_block"),  // 锚点是青金石块
        'S' to StringStateMatcher.parse("minecraft:stone_bricks")
    )
)
```

### 覆盖空格默认行为

默认空格表示"必须为空气"。如果空格位置不需要检测，将其覆盖为 ANY：

```kotlin
val dome = DenseMultiblock(
    pattern = arrayOf(
        arrayOf(
            "GGGGGGG",
            "GGG GGG",
            "GG   GG",
            "G     G",
            "GG   GG",
            "GGG GGG",
            "GGGGGGG"
        ),
        arrayOf(
            "RRRSRRR",
            "RRSSSRR",
            "RSSSSSR",
            "SSS0SSS",
            "RSSSSSR",
            "RRSSSRR",
            "RRRSRRR"
        )
    ),
    mapping = mapOf(
        ' ' to StateMatcher.ANY,         // 空格 = 任意方块（不检测）
        '0' to StringStateMatcher.parse("minecraft:lapis_block"),
        'G' to StringStateMatcher.parse("minecraft:purple_stained_glass"),
        'R' to StringStateMatcher.parse("minecraft:stone"),
        'S' to StringStateMatcher.parse("minecraft:sponge"),
    )
).apply { symmetrical = true }
```

### 带朝向属性的方块

方块的 `facing` 等状态属性在旋转时很关键：

```kotlin
val staircase = DenseMultiblock(
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
        'N' to StringStateMatcher.parse("minecraft:oak_stairs[facing=south]"),
        'S' to StringStateMatcher.parse("minecraft:birch_stairs[facing=north]"),
        'W' to StringStateMatcher.parse("minecraft:brick_stairs[facing=east]"),
        'E' to StringStateMatcher.parse("minecraft:stone_brick_stairs[facing=west]")
    )
)
```

### 包含流体和含水方块

```kotlin
val fluidStructure = DenseMultiblock(
    pattern = arrayOf(
        arrayOf(
            "GG   GG",
            " LLLLLG",
            "GGGGGGG",
            " WW0WWG",
            "GGGGGGG",
            " SSSSSG",
            "GG   GG"
        )
    ),
    mapping = mapOf(
        ' ' to StateMatcher.ANY,
        '0' to StringStateMatcher.parse("minecraft:lapis_block"),
        'G' to StringStateMatcher.parse("minecraft:bricks"),
        'W' to StringStateMatcher.parse("minecraft:water"),
        'L' to StringStateMatcher.parse("minecraft:lava"),
        'S' to StringStateMatcher.parse("minecraft:brick_slab[type=bottom,waterlogged=true]"),
    )
)
```

## 注意事项

| 场景 | 说明 |
|------|------|
| 未找到 `0` | 锚点默认为 `(0, 0, 0)` 即图案最底层西北角 |
| 多个 `0` | 使用最后一个找到的位置 |
| 行长度不一致 | 短行末尾自动填充空格（空气） |
| 未映射的字符 | 会映射为 `StateMatcher.AIR`（空气），应确保所有字符都有映射 |
| `ANY` 匹配器 | 在 `simulate()` 中被跳过，不产生 `SimulateResult` |
| 结构尺寸 | 由 `pattern` 自动推断，通过 `size` 属性获取 |

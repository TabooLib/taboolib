# minecraft-multiblocks

多方块结构模块，提供在 Minecraft 世界中定义、验证和匹配多方块结构的能力。参考 [Patchouli](https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/multiblocks/) 的 Multiblock 设计，适配 TabooLib/Bukkit 生态。

## 依赖

```kotlin
compileOnly(project(":module:minecraft:minecraft-multiblocks"))
```

## 核心概念

### 什么是多方块结构

多方块结构（Multiblock）是由多个方块按照特定排列组成的结构。模块提供：

- **结构定义** — 用图案或坐标描述结构由哪些方块组成
- **结构验证** — 检测世界中某个位置是否存在该结构（支持自动旋转检测）
- **结构模拟** — 计算结构中每个方块的世界坐标（用于预览、进度提示等）

### 两种定义方式

| 类型 | 适用场景 | 定义方式 |
|------|---------|---------|
| `DenseMultiblock` | 紧凑结构、方块密度高 | 字符图案（`String[][]`），类似合成配方 |
| `SparseMultiblock` | 大型稀疏结构、不规则形状 | 坐标映射（`Map<BlockPos, IStateMatcher>`） |

两种方式在功能上完全等价——都支持验证、模拟和旋转检测。选择哪种取决于结构的形状特征。

---

## 密集型（DenseMultiblock）

密集型使用二维字符串数组定义结构，类似 Minecraft 合成配方的格式。每个字符代表一个方块，通过 `mapping` 映射到具体的方块匹配器。

### 基础用法

```kotlin
// 定义一个 3x2x3 的石砖祭坛
val altar = DenseMultiblock(
    pattern = arrayOf(
        // 第一个数组 = 最顶层（最高 Y）
        arrayOf(
            "   ",
            " S ",
            "   "
        ),
        // 最后一个数组 = 最底层（最低 Y）
        arrayOf(
            "SSS",
            "S0S",    // '0' = 中心锚点
            "SSS"
        )
    ),
    mapping = mapOf(
        'S' to StringStateMatcher.parse("minecraft:stone_bricks")
    )
)
```

### 图案坐标系

图案是一个三维结构，索引含义如下：

```
pattern = arrayOf(           ← 外层数组：Y 层
    arrayOf(                 ←   中层数组：Z 行（北→南）
        "ABCDE",             ←     字符串字符：X 列（西→东）
        "FGHIJ",
        "KLMNO"
    ),
    ...
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

### 特殊字符

| 字符 | 含义 | 默认匹配器 | 可否覆盖 |
|------|------|-----------|---------|
| `0` | **中心锚点**（必须恰好出现一次） | `StateMatcher.AIR` | 可在 mapping 中覆盖 |
| `_` | 任意方块（不参与验证） | `StateMatcher.ANY` | 可覆盖 |
| ` ` (空格) | 空气 | `StateMatcher.AIR` | 可覆盖 |

**重要规则：**
- 图案中 **必须恰好包含一个 `0` 字符** 作为锚点
- 图案中出现的所有其他字符（除 `0`、`_`、空格外）**必须在 mapping 中定义**
- 特殊字符的默认行为可以在 mapping 中覆盖，例如 `'0' to StringStateMatcher.parse("minecraft:lapis_block")` 可使锚点位置要求为青金石块

### 大型多方块示例

```kotlin
// 一个 7x5x7 的穹顶结构（参考 Patchouli 测试用例）
val dome = DenseMultiblock(
    pattern = arrayOf(
        // 第 1 层（最顶层）
        arrayOf(
            "GGGGGGG",
            "GGG GGG",
            "GG   GG",
            "G     G",
            "GG   GG",
            "GGG GGG",
            "GGGGGGG"
        ),
        // 第 2 层
        arrayOf(
            "GGG GGG",
            "GG   GG",
            "G     G",
            "       ",
            "G     G",
            "GG   GG",
            "GGG GGG"
        ),
        // 第 3 层
        arrayOf(
            "GG   GG",
            "G     G",
            "       ",
            "       ",
            "       ",
            "G     G",
            "GG   GG"
        ),
        // 第 4 层（中间有箱子）
        arrayOf(
            "GGG GGG",
            "GG   GG",
            "G     G",
            "   C   ",
            "G     G",
            "GG   GG",
            "GGG GGG"
        ),
        // 第 5 层（底层，包含锚点）
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
        ' ' to StateMatcher.ANY,                                          // 覆盖空格为"任意方块"
        '0' to StringStateMatcher.parse("minecraft:lapis_block"),         // 自定义锚点方块
        'G' to StringStateMatcher.parse("minecraft:purple_stained_glass"),
        'R' to StringStateMatcher.parse("minecraft:stone"),
        'S' to StringStateMatcher.parse("minecraft:sponge"),
        'C' to StringStateMatcher.parse("minecraft:chest"),
    )
).apply { symmetrical = true }
```

### 带方块属性的朝向结构

方块属性（如楼梯的 `facing`）在旋转检测中非常重要：

```kotlin
// 四方向楼梯平台
val stairPlatform = DenseMultiblock(
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
        'S' to StringStateMatcher.parse("minecraft:oak_stairs[facing=north]"),
        'W' to StringStateMatcher.parse("minecraft:oak_stairs[facing=east]"),
        'E' to StringStateMatcher.parse("minecraft:oak_stairs[facing=west]"),
    )
)
```

### 包含流体的结构

流体方块和含水方块同样可以作为匹配条件：

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

---

## 稀疏型（SparseMultiblock）

稀疏型使用坐标到匹配器的映射定义结构，只需列出有方块的位置。适合大型但方块稀疏的结构（如四角立柱、大型框架等）——不需要定义大量的空气/任意方块。

### 基础用法

```kotlin
// 十字形结构
val cross = SparseMultiblock(
    blocks = mapOf(
        BlockPos(0, 0, 0) to StringStateMatcher.parse("minecraft:diamond_block"),
        BlockPos(1, 0, 0) to StringStateMatcher.parse("minecraft:iron_block"),
        BlockPos(-1, 0, 0) to StringStateMatcher.parse("minecraft:iron_block"),
        BlockPos(0, 0, 1) to StringStateMatcher.parse("minecraft:iron_block"),
        BlockPos(0, 0, -1) to StringStateMatcher.parse("minecraft:iron_block"),
    )
)
```

### 坐标说明

- 所有位置**相对于锚点 `(0, 0, 0)`**
- 锚点就是验证时传入的 `anchor` 位置
- 未定义的位置**不参与验证**（等效于 `ANY`）
- 如果需要某个位置必须为空气，显式添加 `BlockPos(...) to StateMatcher.AIR`

### 大型稀疏结构示例

```kotlin
// 四角立柱（10x4x10 的空间中只有 4 根柱子 + 中心标记）
val fourPillars = SparseMultiblock(
    blocks = buildMap {
        val pillarBlock = StringStateMatcher.parse("minecraft:stone_bricks")
        // 四个角的 3 格高立柱
        for (x in listOf(-5, 5)) {
            for (z in listOf(-5, 5)) {
                for (y in 0..2) {
                    put(BlockPos(x, y, z), pillarBlock)
                }
            }
        }
        // 立柱顶部的雕花石砖
        val capBlock = StringStateMatcher.parse("minecraft:chiseled_stone_bricks")
        for (x in listOf(-5, 5)) {
            for (z in listOf(-5, 5)) {
                put(BlockPos(x, 3, z), capBlock)
            }
        }
        // 中心锚点
        put(BlockPos(0, 0, 0), StringStateMatcher.parse("minecraft:diamond_block"))
    }
)
```

> 如果用 DenseMultiblock 定义同样的结构，需要填写 11x4x11 = 484 个字符，其中绝大多数是空格。稀疏型只需定义 21 个有效位置。

### 密集型 vs 稀疏型对比

| 特性 | DenseMultiblock | SparseMultiblock |
|------|----------------|-----------------|
| 定义方式 | 字符图案 `String[][]` | 坐标映射 `Map<BlockPos, IStateMatcher>` |
| 锚点 | `'0'` 字符位置 | 默认 `(0,0,0)` |
| 空气检测 | 空格字符自动检测空气 | 需显式指定 `StateMatcher.AIR` |
| 未定义位置 | 不存在"未定义"（图案覆盖整个包围盒） | 未定义 = 不检测（ANY） |
| 适合场景 | 紧凑、规则、方块密度高 | 大型、稀疏、不规则 |
| 可读性 | 图案直观可视 | 坐标列表，适合程序生成 |

---

## 状态匹配器

状态匹配器（`IStateMatcher`）定义了"某个位置应该是什么方块"的规则。

### 内置匹配器

| 匹配器 | 说明 |
|--------|------|
| `StateMatcher.ANY` | 匹配任意方块（包括空气），等效于不检测 |
| `StateMatcher.AIR` | 仅匹配空气方块 |

### 工厂方法

```kotlin
// 匹配指定材质（忽略方块状态属性）
StateMatcher.fromMaterial(Material.STONE)

// 精确匹配方块数据（包括状态属性）
val data = Bukkit.createBlockData("minecraft:oak_stairs[facing=north,half=bottom]")
StateMatcher.fromBlockData(data)

// 自定义谓词
StateMatcher.fromPredicate("iron_or_gold") { block ->
    block.type == Material.IRON_BLOCK || block.type == Material.GOLD_BLOCK
}

// 仅用于显示（始终返回 true）
StateMatcher.displayOnly("decorative")
```

### 字符串解析匹配器

`StringStateMatcher.parse()` 支持三种格式：

| 格式 | 示例 | 说明 |
|------|------|------|
| 材质名 | `minecraft:stone` | 匹配材质，忽略状态属性 |
| 方块数据 | `minecraft:oak_stairs[facing=north,half=bottom]` | 精确匹配，包含状态属性 |
| 方块标签 | `#minecraft:wool` | 匹配标签中的所有方块（如所有颜色的羊毛） |

```kotlin
StringStateMatcher.parse("minecraft:stone")
StringStateMatcher.parse("minecraft:oak_stairs[facing=north]")
StringStateMatcher.parse("#minecraft:planks")   // 所有木板
StringStateMatcher.parse("#minecraft:logs")     // 所有原木
```

### 自定义匹配器

实现 `IStateMatcher` 接口：

```kotlin
class LightLevelMatcher(private val minLight: Int) : IStateMatcher {
    override val displayName = "light>=$minLight"
    override fun test(block: Block): Boolean {
        return block.lightLevel >= minLight
    }
}
```

---

## 验证结构

### 自动旋转验证

`validate(world, anchor)` 对非对称结构自动尝试 4 个旋转方向，返回第一个匹配的旋转：

```kotlin
val anchor = BlockPos(block.x, block.y, block.z)

val rotation = multiblock.validate(world, anchor)
if (rotation != null) {
    // 结构匹配！rotation 为匹配时的旋转方向
}
```

### 指定旋转验证

```kotlin
val matched = multiblock.validate(world, anchor, MultiblockRotation.NONE)
```

### 单方块测试

```kotlin
val blockOk = multiblock.test(world, anchor, 1, 0, 0, MultiblockRotation.NONE)
```

### 旋转方向

| 枚举值 | 角度 | 坐标变换 `(x, y, z)` → |
|--------|------|------------------------|
| `NONE` | 0° | `(x, y, z)` |
| `CLOCKWISE_90` | 顺时针 90° | `(-z, y, x)` |
| `CLOCKWISE_180` | 180° | `(-x, y, -z)` |
| `COUNTERCLOCKWISE_90` | 逆时针 90° | `(z, y, -x)` |

旋转围绕 Y 轴进行（水平旋转），Y 坐标不变。

### 对称优化

如果结构在水平面上关于中心轴对称（如正方形平台），设置 `symmetrical = true` 可跳过多余的旋转检查：

```kotlin
multiblock.symmetrical = true
// 验证时只检查 1 个旋转，性能提升约 4 倍
```

---

## 模拟

`simulate()` 返回结构中所有方块的世界坐标和匹配器，**不访问世界**——纯坐标计算。

```kotlin
val results = multiblock.simulate(anchor, MultiblockRotation.NONE)
for (result in results) {
    val pos = result.worldPosition       // 世界坐标
    val matcher = result.stateMatcher    // 匹配器
    val char = result.character          // 图案字符（仅 Dense 有值）
}
```

**用途：**
- **预览高亮** — 在世界中高亮显示结构方块位置
- **进度检测** — 逐个检查哪些方块已放置、哪些缺失
- **放置引导** — 提示玩家每个位置需要什么方块

> 注意：`StateMatcher.ANY` 匹配器在 simulate 中会被跳过（不产生 SimulateResult），因为"任意方块"不需要检测。

---

## 注册与管理

使用 `MultiblockRegistry` 进行全局注册，建议使用 `命名空间:名称` 格式的 ID：

```kotlin
// 注册
MultiblockRegistry.register("my_plugin:altar", altar)

// 获取
val mb = MultiblockRegistry.get("my_plugin:altar")

// 移除
MultiblockRegistry.unregister("my_plugin:altar")

// 获取所有
val all = MultiblockRegistry.getAll()

// 清空
MultiblockRegistry.clear()
```

---

## 完整示例

### 示例 1：多方块熔炉

```kotlin
import taboolib.module.multiblocks.*

// 定义 3x3x3 多方块熔炉（中心镂空，底部实心）
val blastFurnace = DenseMultiblock(
    pattern = arrayOf(
        // 顶层
        arrayOf(
            "BBB",
            "B B",
            "BBB"
        ),
        // 中间层（包含锚点）
        arrayOf(
            "BBB",
            "B0B",
            "BBB"
        ),
        // 底层
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

MultiblockRegistry.register("my_plugin:blast_furnace", blastFurnace)
```

### 示例 2：监听玩家交互检测结构

```kotlin
@SubscribeEvent
fun onInteract(e: PlayerInteractEvent) {
    if (e.action != Action.RIGHT_CLICK_BLOCK) return
    val block = e.clickedBlock ?: return
    val anchor = BlockPos(block.x, block.y, block.z)

    for ((id, multiblock) in MultiblockRegistry.getAll()) {
        val rotation = multiblock.validate(block.world, anchor)
        if (rotation != null) {
            e.player.sendMessage("检测到结构: $id (旋转: $rotation)")
            // 获取所有方块位置
            val results = multiblock.simulate(anchor, rotation)
            // ... 进一步处理
            return
        }
    }
}
```

### 示例 3：进度检测（显示缺失方块）

```kotlin
fun checkProgress(world: World, multiblock: IMultiblock, anchor: BlockPos, rotation: MultiblockRotation) {
    val results = multiblock.simulate(anchor, rotation)
    var completed = 0
    val missing = mutableListOf<SimulateResult>()

    for (result in results) {
        val pos = result.worldPosition
        val block = world.getBlockAt(pos.x, pos.y, pos.z)
        if (result.stateMatcher.test(block)) {
            completed++
        } else {
            missing.add(result)
        }
    }

    println("进度: $completed/${results.size}")
    for (m in missing) {
        println("  缺失: ${m.worldPosition} 需要 ${m.stateMatcher.displayName}")
    }
}
```

---

## API 参考

### 核心类一览

| 类 | 说明 |
|---|---|
| `IMultiblock` | 多方块结构核心接口 |
| `IStateMatcher` | 方块状态匹配器接口 |
| `AbstractMultiblock` | 抽象基类，实现旋转验证逻辑 |
| `DenseMultiblock` | 密集型实现（字符图案） |
| `SparseMultiblock` | 稀疏型实现（坐标映射） |
| `StateMatcher` | 内置匹配器工厂（ANY / AIR / fromMaterial / fromBlockData / fromPredicate） |
| `StringStateMatcher` | 字符串解析匹配器（材质名 / 方块属性 / 标签） |
| `MultiblockRegistry` | 全局注册表 |
| `BlockPos` | 整数坐标，支持旋转和加减运算 |
| `MultiblockRotation` | 旋转方向枚举（4 方向） |

### IMultiblock 接口方法

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `validate(world, anchor)` | `MultiblockRotation?` | 自动旋转验证，返回匹配的旋转方向或 `null` |
| `validate(world, anchor, rotation)` | `Boolean` | 指定旋转方向验证 |
| `simulate(anchor, rotation)` | `List<SimulateResult>` | 模拟结构，返回所有方块位置和匹配器 |
| `test(world, anchor, x, y, z, rotation)` | `Boolean` | 测试单个方块是否匹配 |
| `offset(x, y, z)` | `IMultiblock` | 设置锚点偏移（链式调用） |

### SimulateResult 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `worldPosition` | `BlockPos` | 方块在世界中的实际坐标 |
| `stateMatcher` | `IStateMatcher` | 该位置的匹配规则 |
| `character` | `Char?` | 图案中对应的字符（仅 DenseMultiblock 有值） |

# 稀疏型多方块 SparseMultiblock

## 构造函数

```kotlin
class SparseMultiblock(
    blocks: Map<BlockPos, IStateMatcher>
) : AbstractMultiblock()
```

**参数说明：**

- `blocks`：相对位置到状态匹配器的映射，所有位置相对于锚点 `(0, 0, 0)`

## 坐标说明

- 所有位置**相对于锚点 `(0, 0, 0)`**
- 锚点就是验证时传入的 `anchor` 参数位置
- 未定义的位置**不参与验证**（等效于 `StateMatcher.ANY`）
- 如果需要某个位置必须为空气，需显式添加 `BlockPos(...) to StateMatcher.AIR`

## 基础示例

### 十字形结构

```kotlin
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

### 立柱结构

```kotlin
val pillar = SparseMultiblock(
    blocks = mapOf(
        BlockPos(0, 0, 0) to StringStateMatcher.parse("minecraft:stone_bricks"),
        BlockPos(0, 1, 0) to StringStateMatcher.parse("minecraft:stone_bricks"),
        BlockPos(0, 2, 0) to StringStateMatcher.parse("minecraft:stone_bricks"),
        BlockPos(0, 3, 0) to StringStateMatcher.parse("minecraft:chiseled_stone_bricks"),
    )
)
```

### 大型稀疏结构（程序生成）

```kotlin
// 四角立柱（10x4x10 的空间中只有 4 根柱子 + 中心标记）
val fourPillars = SparseMultiblock(
    blocks = buildMap {
        val pillarBlock = StringStateMatcher.parse("minecraft:stone_bricks")
        val capBlock = StringStateMatcher.parse("minecraft:chiseled_stone_bricks")
        // 四个角的 3 格高立柱
        for (x in listOf(-5, 5)) {
            for (z in listOf(-5, 5)) {
                for (y in 0..2) {
                    put(BlockPos(x, y, z), pillarBlock)
                }
                put(BlockPos(x, 3, z), capBlock) // 顶部装饰
            }
        }
        // 中心锚点
        put(BlockPos(0, 0, 0), StringStateMatcher.parse("minecraft:diamond_block"))
    }
)
```

> 如果用 DenseMultiblock 定义同样的结构，需要填写 11x4x11 = 484 个字符，其中绝大多数是空格。稀疏型只需定义 21 个有效位置。

### 需要空气检测的稀疏结构

```kotlin
// 中心必须是钻石块，周围必须是空气
val isolated = SparseMultiblock(
    blocks = mapOf(
        BlockPos(0, 0, 0) to StringStateMatcher.parse("minecraft:diamond_block"),
        // 显式要求周围为空气
        BlockPos(1, 0, 0) to StateMatcher.AIR,
        BlockPos(-1, 0, 0) to StateMatcher.AIR,
        BlockPos(0, 0, 1) to StateMatcher.AIR,
        BlockPos(0, 0, -1) to StateMatcher.AIR,
        BlockPos(0, 1, 0) to StateMatcher.AIR,
        BlockPos(0, -1, 0) to StateMatcher.AIR,
    )
)
```

## 与 DenseMultiblock 的对比

| 特性 | DenseMultiblock | SparseMultiblock |
|------|----------------|-----------------|
| 定义方式 | 字符图案 `String[][]` | 坐标映射 `Map<BlockPos, IStateMatcher>` |
| 锚点 | `'0'` 字符位置 | 默认 `(0,0,0)` |
| 空气检测 | 空格字符自动检测空气 | 需显式添加 `StateMatcher.AIR` |
| 未定义位置 | 不存在"未定义"（图案覆盖整个包围盒） | 未定义 = 不检测（ANY） |
| 适合场景 | 紧凑、规则、方块密度高 | 大型、稀疏、不规则 |
| 可读性 | 图案直观可视 | 坐标列表，适合程序生成 |
| 内存效率 | 存储整个包围盒 | 只存储有效方块 |

## 注意事项

| 场景 | 说明 |
|------|------|
| 空 blocks | `size` 为 `BlockPos.ZERO`，验证始终返回成功 |
| 不需要空气检测 | 稀疏型中未定义的位置不参与验证，等效于 ANY |
| 需要空气检测 | 显式添加 `BlockPos(...) to StateMatcher.AIR` |
| 对称性 | 同样支持 `symmetrical = true` 优化 |
| 程序生成 | 配合 `buildMap { }` 使用，用循环批量添加位置 |

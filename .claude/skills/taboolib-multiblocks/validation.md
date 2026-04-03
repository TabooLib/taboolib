# 结构验证与旋转检测

## 验证方法

### 自动旋转验证

`validate(world, anchor)` 自动尝试所有旋转方向，返回第一个匹配的旋转：

- 非对称结构：按 `NONE` → `CLOCKWISE_90` → `CLOCKWISE_180` → `COUNTERCLOCKWISE_90` 顺序尝试
- 对称结构（`symmetrical = true`）：仅尝试 `NONE`

```kotlin
val anchor = BlockPos(block.x, block.y, block.z)

val rotation = multiblock.validate(world, anchor)
if (rotation != null) {
    // 匹配成功，rotation 为匹配时的旋转方向
    println("结构匹配，旋转: $rotation")
} else {
    // 未匹配任何旋转方向
}
```

### 指定旋转验证

`validate(world, anchor, rotation)` 只检查指定旋转方向：

```kotlin
val matched = multiblock.validate(world, anchor, MultiblockRotation.NONE)
if (matched) {
    // 在 NONE 旋转下匹配成功
}
```

### 单方块测试

`test(world, anchor, x, y, z, rotation)` 测试结构中单个位置是否匹配：

```kotlin
val blockOk = multiblock.test(world, anchor, 1, 0, 0, MultiblockRotation.NONE)
```

如果坐标不在结构范围内，返回 `true`。

## 旋转方向

`MultiblockRotation` 定义了 4 种绕 Y 轴的旋转：

| 枚举值 | 角度 | 坐标变换 `(x, y, z)` → | 方向变化 |
|--------|------|------------------------|---------|
| `NONE` | 0° | `(x, y, z)` | 原始方向 |
| `CLOCKWISE_90` | 顺时针 90° | `(-z, y, x)` | 北→东→南→西 |
| `CLOCKWISE_180` | 180° | `(-x, y, -z)` | 北↔南, 东↔西 |
| `COUNTERCLOCKWISE_90` | 逆时针 90° | `(z, y, -x)` | 北→西→南→东 |

**旋转验证示例：**

```
假设东侧有一个楼梯 facing=west（朝西面放置）
NONE 旋转下它在 anchor 东侧
CLOCKWISE_90 后它应该在 anchor 南侧
```

所有旋转围绕 Y 轴进行（水平旋转），Y 坐标不变。

## 对称优化

如果结构在水平面上关于中心轴对称（如正方形平台），设置对称标志可跳过多余旋转：

```kotlin
// 标记为对称结构
multiblock.symmetrical = true

// 验证时只检查 NONE 旋转，性能提升约 4 倍
val rotation = multiblock.validate(world, anchor)
```

**什么时候设置对称：**
- 正方形平台 — 对称
- 圆形结构 — 对称
- 十字形（四臂相同）— 对称
- L 形结构 — 不对称
- 带朝向的结构 — 通常不对称

## 模拟

`simulate(anchor, rotation)` 返回结构中所有方块的世界坐标，**不访问世界**——纯坐标计算。

```kotlin
val results = multiblock.simulate(anchor, MultiblockRotation.NONE)
for (result in results) {
    println("位置: ${result.worldPosition}")
    println("匹配器: ${result.stateMatcher.displayName}")
    println("字符: ${result.character}")  // 仅 DenseMultiblock 有值
}
```

**注意：** `StateMatcher.ANY` 匹配器在 simulate 中会被跳过，不产生 `SimulateResult`（因为"任意方块"不需要检测）。

### 用途

| 用途 | 说明 |
|------|------|
| **预览高亮** | 在世界中高亮显示结构方块位置 |
| **进度检测** | 逐个检查哪些方块已放置、哪些缺失 |
| **放置引导** | 提示玩家每个位置需要什么方块 |
| **结构分析** | 统计结构使用了多少种不同方块 |

## 完整示例

### 检测玩家右键的方块

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
            return
        }
    }
}
```

### 进度检测

```kotlin
fun checkProgress(
    world: World,
    multiblock: IMultiblock,
    anchor: BlockPos,
    rotation: MultiblockRotation
): Pair<Int, List<SimulateResult>> {
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

    return completed to missing
}

// 使用
val (done, missing) = checkProgress(world, multiblock, anchor, rotation)
player.sendMessage("进度: $done/${done + missing.size}")
for (m in missing) {
    player.sendMessage("  缺失: ${m.worldPosition} 需要 ${m.stateMatcher.displayName}")
}
```

### 遍历所有注册的多方块

```kotlin
fun findMultiblockAt(world: World, anchor: BlockPos): Pair<String, MultiblockRotation>? {
    for ((id, multiblock) in MultiblockRegistry.getAll()) {
        val rotation = multiblock.validate(world, anchor)
        if (rotation != null) {
            return id to rotation
        }
    }
    return null
}
```

## 注意事项

| 场景 | 说明 |
|------|------|
| 大型结构 | 验证遍历所有方块，结构越大开销越高，建议限制检测频率 |
| `test()` 坐标越界 | 如果坐标不在结构范围内，返回 `true`（不检测） |
| 锚点选择 | 应与结构定义的 `'0'` 位置对应，通常是玩家交互的方块 |
| 旋转与方块属性 | 当前版本不自动旋转方块的 `facing` 等属性，需在匹配器中自行处理 |
| 并发安全 | `MultiblockRegistry` 内部使用 `ConcurrentHashMap`，线程安全 |

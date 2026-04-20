# Incision

Incision 是 TabooLib 的运行时织入模块，目标不是做一个通用 AOP/Mixin 框架，而是给 Bukkit/Paper/NMS 场景提供一套可控、可诊断、可回滚的手术式织入能力。

它同时支持两套入口：

- DSL：`Scalpel { ... }` / `Scalpel.transient { ... }`
- 注解：`@Surgeon` + `@Lead/@Trail/@Splice/...`

推荐策略：

- 默认优先使用注解模式。
- 只有在 patch 生命周期需要运行期动态控制时，才优先考虑 DSL。
- 能稳定写成 `@Surgeon` 的长期 patch，不建议改成 DSL。

这份 README 同时包含：

- 用户文档：怎么声明、怎么织入、怎么调试
- 设计文档：模块分层、调度规则、字节码落点与约束
- 术语对照表
- 内部语法说明表

## 适用场景

适合：

- 在 Bukkit/Paper 插件里做方法入口、出口、调用点级别的轻量织入
- 对 NMS/Bukkit 方法做版本门控、remap 后再织入
- 对 Kotlin `object`、`companion`、`@JvmStatic` 目标做双路径覆盖
- 做临时 patch、范围 patch、线程局部 patch、批量 patch
- 对运行时问题提供可回滚的诊断性织入

默认选型：

- 长期、稳定、随模块启动一起生效的 patch：优先 `@Surgeon`
- 临时、作用域、线程局部、事件驱动或诊断性 patch：再考虑 DSL

不适合：

- 把整套业务逻辑长期建立在大规模字节码改写之上
- 期望它替代完整字节码框架或编译期 mixin 系统
- 在完全不了解目标字节码形态时直接依赖复杂 `InsnPattern`

## 生命周期边界

Incision 当前会尽量把注解式织入前推到 `LifeCycle.CONST`。

- `@Surgeon` 扫描与物理织入发生在 `CONST`
- `INIT / LOAD / ENABLE / ACTIVE` 的宿主行为理论上都可以被更早注册的 patch 命中
- 但插件静态代码块，以及发生在 `ClassVisitorAwake(CONST)` 之前的更早引导行为，仍然不在可拦截范围内

换句话说，`ENABLE` 的确太晚；当前实现已经把可前移的部分推到了这个模块在 TabooLib 里的最前窗口。

## 快速上手

### 1. DSL 方式

这是补充能力，不是默认首选。

```kotlin
import taboolib.module.incision.annotation.SurgeryDesk
import taboolib.module.incision.api.Suture
import taboolib.module.incision.dsl.Scalpel as scalpel

@SurgeryDesk
object DemoDesk {

    val greetPatch: Suture by scalpel {
        lead("top.example.Target#greet(java.lang.String)java.lang.String") { theatre ->
            println("before greet: ${theatre.args[0]}")
        }
    }

    fun patchOnce() {
        scalpel.transient {
            splice("top.example.Target#greet(java.lang.String)java.lang.String") { theatre ->
                theatre.resume.proceed("patched")
            }
        }.use {
            // 作用域内生效
        }
    }
}
```

要点：

- `scalpel {}` 只能放在 `@SurgeryDesk object` 内
- 持久 patch 返回 `Suture`
- `transient/scoped/threadLocal/armOn/disarmOn/exclusive` 都受 `@SurgeryDesk` 调用点检查约束
- 更适合临时 patch、诊断 patch 和动态启停场景，不适合作为常规长期 patch 的首选写法

### 2. 注解方式

这是默认推荐写法。

```kotlin
import taboolib.module.incision.annotation.Lead
import taboolib.module.incision.annotation.Operation
import taboolib.module.incision.annotation.Surgeon

@Surgeon(priority = 50)
object DemoSurgeon {

    @Lead("method:top.example.Target#greet(java.lang.String)java.lang.String")
    @Operation(id = "greet-lead", priority = 100)
    fun beforeGreet(theatre: taboolib.module.incision.api.Theatre) {
        println("before greet: ${theatre.args[0]}")
    }
}
```

要点：

- `@Surgeon` 只能标在 Kotlin `object`
- 扫描期会把方法翻译成 `AdviceEntry`，注册进 dispatcher，再触发织入
- 方法级 `@Operation` 可以覆盖类级默认优先级和启用状态
- 适合稳定、长期、声明式的 patch，是模块对外的首选模式

## Advice 类型总表

| 类型 | DSL/注解 | 典型用途 | 是否替换原逻辑 | 关键约束 |
| --- | --- | --- | --- | --- |
| `Lead` | `lead` / `@Lead` | 入口探针、计数、参数观察 | 否 | 只能表达入口语义 |
| `Trail` | `trail` / `@Trail` | 正常出口或异常出口收尾 | 否 | `onThrow=false` 时不覆盖异常出口 |
| `Splice` | `splice` / `@Splice` | 环绕、短路、改参与放行 | 可选 | 必须显式 `proceed/skip/override` |
| `Graft` | `graft` / `@Graft` | 在锚点前后追加逻辑 | 否 | 原指令仍会执行 |
| `Bypass` | `bypass` / `@Bypass` | 把单个调用点重定向到 handler | 是，替换单点 | 只替换目标位点，不替换整个方法 |
| `Trim` | `trim` / `@Trim` | 改写参数、返回值、局部变量 | 改值，不改流程 | 必须保证值类型兼容 |
| `Excise` | `excise` / `@Excise` | 整段方法覆写 | 是，替换整个方法 | 同一目标只能有一个 Excise |

## Suture 生命周期

| 状态/动作 | 含义 | 典型接口 |
| --- | --- | --- |
| `ARMED` | 已织入并启用 | 初始成功状态 |
| `TRIGGERED` | 已触发过一次以上 | 运行统计态 |
| `SUSPENDED` | 字节码仍在，但 dispatcher 跳过 handler | `suspend()` |
| `HEALED` | 已卸载或回滚 | `heal()` / `close()` |
| `INACTIVE_UNRESOLVED` | 声明未成功解析 | 解析失败场景 |

控制接口：

- `heal()`：永久卸载
- `suspend()`：临时停用，但不回滚织入点
- `resume()`：恢复已挂起的 patch
- `close()`：等价于 `heal()`

## DSL 模式

| 模式 | 作用 | 说明 |
| --- | --- | --- |
| `scalpel {}` | 持久 patch | 通常作为属性委托，返回 `Suture` |
| `scalpel.deferred {}` | 惰性 patch | 延迟到首次访问或目标类加载后 arm |
| `scalpel.transient {}` | 一次性 patch | 需手动 `heal` 或 `use` |
| `scalpel.scoped {}` | 作用域 patch | 块内生效，块外自动回收 |
| `scalpel.threadLocal {}` | 线程局部 patch | 默认不启用，按线程激活 |
| `scalpel.armOn/disarmOn` | 事件驱动 patch | 返回 `ArmTrigger`，由调用方决定何时 arm/disarm |
| `scalpel.exclusive` | 互斥 patch | 块内挂起同 target 的其他 ARMED patch |

使用建议：

- 如果 patch 可以在启动期静态声明，优先改写成注解模式。
- 只有 patch 需要按代码路径即时创建、挂起、恢复或销毁时，再使用 DSL。

## 锚点与落点

| `Anchor` | 含义 | 常见用途 |
| --- | --- | --- |
| `HEAD` | 方法入口 | `Lead`、参数 Trim |
| `TAIL` | 正常出口前 | `Trail` 收尾 |
| `RETURN` | return 指令前 | 返回值 Trim |
| `INVOKE` | 方法调用处 | Graft/Bypass 调用点 |
| `FIELD_GET` | 字段读 | 字段读取探针 |
| `FIELD_PUT` | 字段写 | 字段写入探针 |
| `NEW` | `new` 指令 | 构造前后探针 |
| `THROW` | 抛异常处 | 异常路径观察 |

`Site` 参数：

| 字段 | 说明 |
| --- | --- |
| `anchor` | 锚点类型 |
| `target` | 锚点目标，例如 `owner#name(desc)ret` |
| `shift` | `BEFORE` / `AFTER` |
| `ordinal` | 第几个命中，`-1` 表示全部 |
| `offset` | 相对锚点再移动几条指令 |

## 版本、remap、Kotlin 目标扩展

### `@Version`

- 在扫描期决定某条 advice 是否注册
- 默认 matcher 走 Minecraft/NMS 版本
- 支持自定义 matcher FQCN
- matcher 解析失败会回退到 Noop matcher，这通常意味着该 advice 不会按预期筛选

### remap

- 用户声明可以写逻辑上的 NMS owner/name/desc
- 运行时交给 `RemapRouter` / `TabooLibNmsResolver` 解析到实际类名
- 安装 weaver 时会先做 owner 级映射，再对已加载类做 retransform

### `@KotlinTarget`

- 解决 Kotlin companion 实例方法与 `@JvmStatic` 静态桥接方法是两条调用路径的问题
- 可分别扩展到：
  - `companionInstance`
  - `jvmStaticBridge`

## 诊断与排错

优先看三类信息：

- `Forensics.debug/warn`
- `Trauma.*`
- `Incision-Test` 对应分类用例

常见问题：

| 现象 | 常见原因 | 先看哪里 |
| --- | --- | --- |
| advice 未命中 | 描述符错、scope 过宽或过窄、pattern 不匹配 | `DescriptorCodec`、`Scope`、`InsnPattern` |
| `ResumeMissing` | `Splice` 没有显式放行或短路 | handler 本身 |
| 只命中 Java，不命中 Kotlin | 漏了 companion / `@JvmStatic` 扩展 | `@KotlinTarget` |
| 某些 NMS 版本不生效 | 版本过滤或 remap 结果不一致 | `@Version`、`RemapRouter` |
| 同 target 顺序不对 | 优先级或注册顺序认知错误 | `AdviceChain` 排序规则 |
| 运行时写死 dispatcher 类名失效 | 该类在运行时会被重定向 | 不要硬编码 runtime redirect 目标 |

## 术语对照表

| 术语 | 对外理解 | 代码对应 |
| --- | --- | --- |
| 手术 / patch | 一组对目标方法生效的织入声明 | `Suture` |
| 施术者 | 持有注解式 advice 的 `object` | `@Surgeon` |
| 工作台 | 持有 DSL patch 的 `object` | `@SurgeryDesk` |
| 现场 | advice 执行时看到的上下文 | `Theatre` |
| 放行 | 继续执行原方法或原指令 | `resume.proceed()` |
| 短路 | 不再执行原方法，直接给结果 | `resume.skip()` / `override()` |
| 锚点 | 要插入或替换的字节码位置 | `Anchor` / `Site` |
| 链 | 某个 target 下的 advice 顺序集合 | `AdviceChain` |
| 调度器 | 运行时按 target 分发 advice 的中心 | `TheatreDispatcher` |
| 织入器 | 把 dispatcher 调用写回字节码的组件 | `Scalpel.installWeaver` / `SiteWeaver` |

## 内部语法说明表

### 1. 方法描述符

Incision 内部统一使用：

```text
owner#method(arg1,arg2,...)returnType
```

示例：

| 写法 | 含义 |
| --- | --- |
| `org.bukkit.entity.Player#kickPlayer(java.lang.String)void` | 实例方法 |
| `top.example.Target$Companion#echo(java.lang.String)java.lang.String` | Kotlin companion 实例方法 |
| `net.minecraft.server.MinecraftServer#getPlayerCount()int` | NMS 方法 |

说明：

- `owner` 最终会转成 JVM internal name
- Kotlin companion 与 `@JvmStatic` 可能需要额外 target 扩展
- 描述符错误通常会落到 `Trauma.Declaration.BadDescriptor`

### 2. Scope DSL

| 语法 | 含义 |
| --- | --- |
| `class:com.foo.Bar` | 匹配类 |
| `method:Foo#bar(*)` | 匹配方法 |
| `field:Foo#name:String` | 匹配字段 |
| `&` | 与 |
| `|` | 或 |
| `!` | 非 |

示例：

```text
class:org.bukkit.entity.Player & method:org.bukkit.entity.Player#kickPlayer(*)
```

### 3. `InsnPattern` / `Step`

| 字段 | 说明 |
| --- | --- |
| `opcode` | 目标 opcode，`Op.ANY` 表示任意 |
| `owner` | 调用/字段所属类，支持 glob |
| `name` | 方法名或字段名，支持 glob |
| `desc` | 方法描述符或字段类型，支持 glob |
| `cst` | 常量值约束 |
| `repeat` | 连续重复次数 |

说明：

- 它匹配的是编译后字节码，不是源码
- 常量折叠、编译器优化、Kotlin 桥接方法都会影响结果
- 当前测试矩阵已覆盖 `ICONST/LDC/NEW/INVOKE/GOTO/ARRAYLENGTH/PUTFIELD/...`

### 4. `where` 谓词

`where` 适合做命中后的二次筛选，不替代描述符和锚点。

已覆盖能力：

| 能力 | 示例 |
| --- | --- |
| 字面量 | `true` / `false` / `null` / `1.5` |
| 比较 | `x == "a"`、`n > 3`、`n <= 10` |
| 集合 | `x in ["a","b"]` |
| 类型 | `x is java.lang.String` |
| 属性/方法 | `self.name`、`arg0.length()` |
| 逻辑 | `a && b`、`a || b`、`!(a)` |

建议：

- 先用 descriptor / scope 缩小范围，再用 `where`
- `where` 写复杂时优先加对应测试用例，不要只靠肉眼判断

## 设计文档

### 分层

| 层 | 责任 | 关键文件 |
| --- | --- | --- |
| 声明层 | DSL 与注解声明 | `dsl/Scalpel.kt`、`annotation/*` |
| 扫描层 | 把 `@Surgeon` 方法翻译成运行时条目 | `loader/SurgeonScanner.kt` |
| 注册层 | 维护 patch 生命周期与 target 索引 | `runtime/SurgeryRegistry.kt`、`api/Suture.kt` |
| 调度层 | 在方法命中时执行 advice 链 | `runtime/TheatreDispatcher.kt` |
| 落点层 | 负责 `SiteSpec`、pattern、offset 等落点计算 | `weaver/site/*` |
| 织入层 | 对目标类做 retransform 并写入 dispatcher 调用 | `weaver/*`、`loader/*Backend*` |
| 诊断层 | 暴露 warn/debug/Trauma | `diagnostic/*` |

### 运行流程

1. 用户通过 DSL 或 `@Surgeon` 声明 advice。
2. 声明被翻译成 `AdviceEntry`。
3. `AdviceEntry` 注册到 `TheatreDispatcher`。
4. `Scalpel.installWeaver` 按 owner 聚合目标并触发 retransform。
5. 织入器把 dispatcher 调用写进目标字节码。
6. 运行期命中目标方法时，dispatcher 按 target 拉出 `AdviceChain` 执行。
7. `Suture` 负责这组 advice 的启停、挂起和卸载。

### 排序规则

- 按 `priority` 降序
- 同优先级保持注册顺序
- 类级 `@Surgeon(priority)` 是默认值
- 方法级 `@Operation(priority)` 可覆盖类级默认值

### 关键设计约束

#### 1. 先 dispatcher，再 handler

Incision 不把业务 handler 直接写进字节码，而是只写 dispatcher 调用。这样能保证：

- 运行时可以统一做 enable/disable/suspend/resume
- advice 注册与卸载可集中管理
- 诊断入口稳定

#### 2. 先 owner 聚合，再 retransform

同一个 owner 上的 advice 会先聚合后再织入，避免重复 retransform 带来额外抖动。

#### 3. classloader 与 remap 必须一起考虑

这个模块天然有两个不稳定维度：

- classloader
- NMS/Bukkit remap

因此：

- 不要直接硬编码可能被运行时重定向的类名
- 平台可直连时优先 import 平台 API，再由平台启用条件决定是否生效
- 运行时 owner 以 `RemapRouter.resolveOwner` 结果为准

#### 4. `InsnPattern` 是能力，不是默认路径

字节码序列匹配最强，但也最脆。能用明确描述符和 `Site` 解决的问题，不应先上复杂 pattern。

### 与测试矩阵的对应关系

| 设计面 | 主要测试分类 |
| --- | --- |
| DSL 生命周期 | 基础 DSL |
| 注解扫描与翻译 | Surgeon 注解 |
| Kotlin companion / `@JvmStatic` / operation 元信息 | 元信息与 Kotlin |
| 版本门控 / remap / Bukkit / NMS / 跨 CL | 平台与版本 |
| 谓词 / opcode 序列 / offset | 谓词与字节码 |
| anchor / site / trim / trauma 诊断 | 锚点矩阵与诊断 |

## 选型建议

优先级顺序：

1. 先判断能否写成 `@Surgeon` + 注解
2. 只有在生命周期必须动态化时，才退到 DSL

优先选注解模式的典型场景：

- 插件启动后就应长期存在的 patch
- 可以稳定写死 target/scope/site 的 patch
- 希望声明和行为集中在一个 `object` 中，便于扫描、排序和维护

适合用 DSL 的典型场景：

- `transient` 一次性 patch
- `scoped` 块级 patch
- `threadLocal` 线程级 patch
- `armOn/disarmOn` 事件驱动 patch
- 调试、压测、临时诊断和人工操作型 patch

## 维护建议

- 修改 DSL 或注解语义时，同时更新：
  - 本 README
  - `annotation/*` KDoc
  - `Incision-Test` 对应用例与 `CaseDocs`
- 修改 bytecode matching、offset、anchor 逻辑时，优先跑对应矩阵，而不是只看单个 case
- 看到运行时 warning 时，先判断它是不是测试刻意覆盖的 fallback 样本，再决定是否修复

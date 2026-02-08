# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**语言约定：所有对话和输出请使用中文。**

## 项目概述

TabooLib 是 Minecraft（Java 版）跨平台插件开发框架，基于 Kotlin 构建，支持 Bukkit、BungeeCord、Velocity、AfyBroker 等平台。当前版本 6.2.2，分支 `dev/6.2.0`。

## 构建命令

```bash
# 构建整个项目
./gradlew build

# 构建单个模块（示例）
./gradlew :common-util:build
./gradlew :module:bukkit:bukkit-ui:build
./gradlew :platform:platform-bukkit-impl:build

# 发布到本地 Maven
./gradlew publishToMavenLocal

# 发布开发版本
./gradlew build -Pdev
./gradlew build -PdevLocal
```

## 架构概览

### 模块层次

```
common/              → 引导启动、隔离类加载器（IsolatedClassLoader）、生命周期管理
common-env/          → 运行时环境、依赖下载与重定位
common-util/         → 工具函数、内部事件总线、反射工具
common-platform-api/ → 平台抽象层（ProxyPlayer、命令系统、事件系统、调度器）
common-legacy-api/   → 向后兼容 API
module/              → 功能模块（按 basic/bukkit/bukkit-nms/minecraft/database/script 分类）
platform/            → 各平台的具体实现（bukkit-impl、bungee-impl、velocity-impl 等）
```

### 生命周期

框架按以下阶段顺序初始化：
`NONE → CONST → INIT → LOAD → ENABLE → ACTIVE → DISABLE`

### 核心注解

- `@Awake` — 自动初始化，绑定到生命周期阶段
- `@PlatformSide` — 标记代码所属平台（BUKKIT / BUNGEE / VELOCITY / AFYBROKER / APPLICATION）
- `@Inject` — 依赖注入
- `@SubscribeEvent` — 事件监听器注册

### 隔离类加载

使用 `IsolatedClassLoader` 沙箱化加载依赖，通过 Shadow 插件重定位 `org.tabooproject` → `taboolib.library`，防止与服务端/其他插件的依赖冲突。

### 平台抽象

`common-platform-api` 定义跨平台接口（ProxyPlayer、ProxyCommandSender 等），各 `platform/*-impl` 模块提供具体实现。通过服务提供者模式（PlatformService）在运行时绑定。

### 模块加载流程

1. IsolatedClassLoader 引导启动
2. PrimitiveLoader 加载模块
3. 下载并重定位 TabooLib 模块
4. 执行模块入口点（定义在 `extra.properties`）
5. 初始化 Kotlin 环境
6. 注册类访问器和回调

## 技术约束

- **Java 8 兼容**：`jvmTarget = 1.8`，`sourceCompatibility = 1.8`
- **Kotlin 1.8.22**，启用 `-Xjvm-default=all`
- **包结构**：所有代码在 `taboolib.*` 包下（`taboolib.common.*`、`taboolib.platform.*`、`taboolib.module.*`）
- **Maven 坐标**：`io.izzel.taboolib:{模块名}:{版本}`
- **不可发布的模块**：名为 `module`、`platform`、`expansion` 的父项目以及 `impl` 开头的模块不参与 Maven 发布

## 关键依赖

- Kotlin stdlib、kotlinx-coroutines-core 1.7.3
- Google Guava 21.0、Gson 2.8.7
- Apache Commons Lang3 3.5
- TabooProject Reflex 1.1.8（反射库）
- Shadow 7.1.2（JAR 打包与重定位）

## 外部文档

- 官方文档：https://docs.tabooproject.org
- 构件仓库：https://repo.tabooproject.org
- 非官方文档（枫溪）：https://taboolib.feishu.cn/wiki/Lzf8wFEsfiHclskCuGkctoUNn9b

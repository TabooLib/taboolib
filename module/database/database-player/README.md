# database-player

`database-Player` 其中提供了一个抽象的数据库管理类 `PlayerDatabase`
---

## 快速开始

### 1. 继承 `PlayerDatabase`

创建自己的数据库类时，可以通过继承 `PlayerDatabase` 来管理玩家数据的加载与释放。

示例代码：

```kotlin
object TestDatabase : PlayerDatabase() {

    /**
     * 玩家加入服务器时，加载玩家数据容器
     */
    @SubscribeEvent
    fun onJoin(ev: PlayerJoinEvent) {
        submitAsync {
            QuestDatabase.setupDataContainer(ev.player.uniqueId)
        }
    }

    /**
     * 玩家退出服务器时，释放玩家数据容器
     */
    @SubscribeEvent
    fun onQuit(ev: PlayerQuitEvent) {
        QuestDatabase.releaseDataContainer(ev.player.uniqueId)
    }

}
```

### 2. 配置数据库连接

在项目的配置文件中，定义数据库的连接信息（如 MySQL 配置），以便在代码中加载这些信息来连接数据库。下面是一个 YAML 格式的示例配置：

```yaml
database:
  enable: true
  host: localhost
  port: 3306
  user: root
  password: root
  database: database_name
  table: table_name
```

> **注意**：当 `enable` 为 `false` 时，将使用 SQLite 数据库；设置为 `true` 时将使用 MySQL 数据库。

### 3. 初始化数据库

在启动的生命周期中初始化数据库，配置文件和数据库路径可以根据需要进行调整。

示例代码：

```kotlin
@Awake(LifeCycle.ENABLE)
fun enable() {
    // 使用配置初始化数据库
    TestDatabase.init(config, "database_test", "data/test.db")
}
```

## 操作数据

使用 `getDataContainer` 方法获取玩家的数据容器并对其进行操作，例如查询、删除和更新数据。

```kotlin
fun test(player: Player){
    val container = TestDatabase.getDataContainer(adaptPlayer(player))
    //查询
    container["Key"]
    // 删除
    container["Key"] = ""
    // 修改
    container["Key"] = "test"
}
```

## 主要功能

- **配置驱动**：支持通过外部配置文件管理数据库连接参数，方便调整和部署。
- **支持多种数据库**：可以根据项目需求使用不同的数据库类型（如 MySQL、SQLite）。

## 扩展说明

- **生命周期管理**：在生命周期的 `ENABLE` 阶段初始化数据库，确保在玩家加入事件之前准备好数据库连接。

## 相关链接

更多信息，请访问 [TabooLib 非官方文档](https://taboolib.feishu.cn/wiki/Px6BwVpXEipwp0ksTHtcAgg9nLd)。
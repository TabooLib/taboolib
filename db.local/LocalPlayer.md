# 本地数据（玩家）
> 本地玩家数据托管系统

## 作用

使用该工具可以自动化管理本地玩家数据文件

## 读取
```java
FileConfiguration file = LocalPlayer.get("BlackSKY");
```

> 所有玩家的数据都会储存在 `plugins/TabooLib/playerdata` 目录下

## 写入
```java
FileConfiguration file = LocalPlayer.get("BlackSKY");
file.set("key", "value");
```

> 该动作会写入缓存而不是立即写入磁盘

## 不可重载

当 `LocalPlayer` 第一次调用玩家数据时读取文件并写入缓存，随后所有操作都会在缓存下进行。  
目前不可以通过任何方式来重载玩家数据。

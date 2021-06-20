# 本地数据

> 本地文件数据托管系统

## 作用

使用该工具可以自动化管理本地数据文件

## 读取

```java
FileConfiguration file = Local.get().get("data.yml");
```

> 该动作会检测缓存而不是实时读取

## 写入

```java
FileConfiguration file = Local.get().get("data.yml");
file.set("key", "value");
```

> 该动作会写入缓存而不是立即写入磁盘

## 重载

```java
Local.get().addFile("data.yml");
```

> 该动作会覆盖缓存中的数据文件

## 函数

在 `Local` 中还提供了一些独特的方法

```java
/**
 * 将所有缓存写入磁盘
 */
public void saveFiles();

/**
 * 将指定插件下的缓存写入磁盘
 */
public void saveFiles(String name);

/**
 * 清空指定插件下的缓存
 */
public void clearFiles(String name);

/**
 * 获取指定插件下的缓存
 */
public LocalPlugin get(String name);
```

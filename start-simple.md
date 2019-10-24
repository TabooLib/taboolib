# 如何使用 TabooLib 5.0
> 推荐使用 IntelliJ IDEA

## 0. 获取 [TabooLib-Loader](http://github.com/bkm016/TabooLib-Loader/releases) 启动类

源码地址
```
https://github.com/Bkm016/TabooLib-Loader/blob/master/src/io/izzel/taboolib/loader/Plugin.java
```
发布地址
```
https://github.com/Bkm016/TabooLib-Loader/releases
```
> 将启动类 `Plugin.java` 复制到你的项目源代码中

## 1. 获取 [TabooLib](https://github.com/Bkm016/TabooLib/releases) 开发依赖

发布地址
```
https://github.com/Bkm016/TabooLib/releases
```
> 将依赖文件添加至开发环境（与添加 BukkitAPI 同理）

## 2 将主类继承启动类

```java
@Plugin.Version(5.08) // 最低 TabooLib 版本要求，省略则不检测版本
public class Main extends Plugin {

    @Override
    public void onStarting() {
        // ...
    }
}
```

> 原有的 `onLoad`, `onEnable`, `onDisable` 方法替换为 `onLoading`, `onStarting`, `onStopping`

## 3. 注意事项

1. 你导入的 TabooLib.jar 或 TabooLib-5.X.jar 不可作为插件直接加载。
2. 启动类的作用是开服之前**下载依赖**，而开发依赖的作用是让你的代码**通过编译**。

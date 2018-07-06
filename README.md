# TabooLib

> Bukkit 开发工具库集合

[![](http://ci.pcd.ac.cn/job/TabooLib/badge/icon)](http://ci.pcd.ac.cn/job/TabooLib)
[![](https://img.shields.io/github/downloads/Bkm016/TabooLib/total.svg)](https://github.com/Bkm016/TabooLib/releases)
[![](https://img.shields.io/github/release/Bkm016/TabooLib.svg)](https://github.com/Bkm016/TabooLib/tags)
[![](https://img.shields.io/github/stars/Bkm016/TabooLib.svg?style=flat-square&label=Stars)](https://github.com/Bkm016/TabooLib)
[![](https://jitpack.io/v/Bkm016/TabooLib.svg)](https://jitpack.io/#Bkm016/TabooLib)

## 插件文档

[TabooLib 文档](https://bkm016.github.io/TabooLib/#/)

## 下载 TabooLib 插件

在 [这里](https://github.com/bkm016/TabooLib/releases) 根据您服务器的网络状态下载**在线**或**离线**版本  
离线版为 **offline** 结尾的 **jar** 文件  
  
如果你的服务器是 **1.7.10** 版本，在 [这里](https://pcd.ac.cn/2018/05/19/71/) 修复因 **SpecialSource** 版本过低导致插件无法载入的问题

## 添加 TabooLib 为库

### Maven 

```xml
<build>
  <repositories>
    <repository>
       <id>jitpack.io</id>
       <url>https://jitpack.io</url>
    </repository>
  </repositories>        
  <dependency>
    <groupId>com.github.Bkm016</groupId>
    <artifactId>TabooLib</artifactId>
    <version>JitPack版本</version>
  </dependency>
</build>
```

### Gradle

```groovy
repositories {
  maven { url 'https://jitpack.io' }
}
dependencies {
  compile 'com.github.Bkm016:TabooLib:JitPack版本'
}
```

### sbt

```scala
resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies += "com.github.Bkm016" % "TabooLib" % "JitPack版本"
```

在添加依赖后，你还需要在 `plugin.yml` 中添加 `softdepend` 或者 `depend` 才能享受到 TabooLib 的全部功能。

---
**3.56** 版本开始 `com.sun.tools.jar` 不再和插件一起发布。  

如果需要启用 *JavaShell* 功能请将 [com.sun.tools.jar](http://skymc.oss-cn-shanghai.aliyuncs.com/plugins/com.sun.tools.jar) 放入 *"TabooLib/JavaShell/lib"* 文件夹中。  

---
**3.832** 版本后开源协议更改为 `MIT`

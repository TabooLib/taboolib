# TabooLib

> Bukkit 开发工具库

[![](http://ci.pcd.ac.cn/job/TabooLibDev/badge/icon)](http://ci.pcd.ac.cn/job/TabooLibDev)
[![](https://img.shields.io/github/downloads/Bkm016/TabooLib/total.svg)](https://github.com/Bkm016/TabooLib/releases)
[![](https://img.shields.io/github/release/Bkm016/TabooLib.svg)](https://github.com/Bkm016/TabooLib/tags)
[![](https://img.shields.io/github/stars/Bkm016/TabooLib.svg?style=flat-square&label=Stars)](https://github.com/Bkm016/TabooLib)
[![](https://jitpack.io/v/Bkm016/TabooLib.svg)](https://jitpack.io/#Bkm016/TabooLib)

## 安装 TabooLib

#### 服务器有网络：
1. 在[这里](https://github.com/bkm016/TabooLib/releases)下载最新版的 **TabooLib**
2. 正常步骤安装
    
#### 服务器无网络：
1. 重复上面步骤
2. 在[这里](https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/libs.rar)下载 **TabooLib** 所需要的第三方库
3. 解压后覆盖 **"plugins"** 文件夹（注意不是丢到 **"plugins"** 文件夹里）

#### 服务端为 1.7.10 版本
1. 重复上面步骤
2. 查阅[文档](https://blog.yumc.pw/posts/Fix-Thermos-Load-Plugin-Class-Not-Found/), 获取解决办法

## 插件文档

[TabooLib 文档](https://bkm016.github.io/TabooLib/#/)

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

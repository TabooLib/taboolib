# TabooLib

> Bukkit 开发工具库集合

[![](http://ci.pcd.ac.cn/job/TabooLib/badge/icon)](http://ci.pcd.ac.cn/job/TabooLib)
[![](https://img.shields.io/github/downloads/Bkm016/TabooLib/total.svg)](https://github.com/Bkm016/TabooLib/releases)
[![](https://img.shields.io/github/release/Bkm016/TabooLib.svg)](https://github.com/Bkm016/TabooLib/tags)
[![](https://img.shields.io/github/stars/Bkm016/TabooLib.svg?style=flat-square&label=Stars)](https://github.com/Bkm016/TabooLib)
[![](https://jitpack.io/v/Bkm016/TabooLib.svg)](https://jitpack.io/#Bkm016/TabooLib)

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

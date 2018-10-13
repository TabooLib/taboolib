# 添加 TabooLib 为库
---

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

### Simple Build Tool

```scala
resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies += "com.github.Bkm016" % "TabooLib" % "JitPack版本"
```

### TabooLibDeprecated
自 TabooLib v4.55 版本起，部分过时或不再维护的工具类被转移至 [TabooLibDeprecated](https://github.com/Bkm016/TabooLib-Deprecated) 项目中。    
如果需要使用这些过时的工具类，则需要根据上面的方法导入 `TabooLib-Deprecated`

!> 您需要在 `plugin.yml` 中添加 `softdepend: TabooLib` 或者 `depend: TabooLib` 才能享受到全部功能。

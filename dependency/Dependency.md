# Dependency

几个简单的注解轻松使用第三方库

## 请求一个第三方库

你只需要在你的主类上加上 `@Dependency` 注解。

比如，假如你要用 Scala 或者 Kotlin，那么可以：

```java
@Dependency(maven = "org.scala-lang:scala-library:2.12.6")
@Dependency(maven = "org.jetbrains.kotlin:kotlin-stdlib:1.2.31")
public class TestMain extends Plugin {
    // ...
}
```

然后这些第三方将在 `onEnable` 调用之前可用。如果需要更早可用，你需要在使用任意第三方库之前调用一次 `TDependencyInjector.inject(instance, instance)`，
`instance` 为你的插件主类实例。

## 使用自定义仓库的库

在 `@Dependency` 里加入 `mavenRepo` 即可，如

```java
@Dependency(maven = "io.papermc:paperlib:1.0.6", mavenRepo = "https://papermc.io/repo/repository/maven-public/")
public class TestMain extends Plugin {
    // ...
}
```

## 使用指定的 URL 的 jar 文件作为库

```java
@Dependency(maven = "com.sun:tools:1.8.0_151", url = "http://skymc.oss-cn-shanghai.aliyuncs.com/plugins/com.sun.tools.jar")
public class TestMain extends Plugin {
    // ...
}
```
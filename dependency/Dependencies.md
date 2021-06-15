# Dependencies

使用多个第三方库

## 请求多个第三方库
```java
@Dependencies({
        @Dependency(maven = "org.scala-lang:scala-library:2.12.6"),
        @Dependency(maven = "org.jetbrains.kotlin:kotlin-stdlib:1.2.31"),
        @Dependency(maven = "io.papermc:paperlib:1.0.6", mavenRepo = "https://papermc.io/repo/repository/maven-public/")
})
public class TestMain extends Plugin {
    // ...
}
```
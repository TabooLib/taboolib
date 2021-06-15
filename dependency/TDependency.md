# TDependency

依赖工具

## 请求一个第三方库
你可以使用 `@Dependency` 注解请求一个第三方库。但是 `@Dependency` 注解请求的第三方库只能在`onEnable`调用之前可用，如果你想更早使用第三方库你就需要 `TDependency#requestLib`，如

```java
public class TestMain extends Plugin {
    @Override
    public void onLoad() {
        try {
            TDependency.requestLib("io.papermc:paperlib:1.0.6", "https://papermc.io/repo/repository/maven-public/", "");
        } catch (ConnectException e) {
            e.printStackTrace();
        }
    }
    // ...
}
```
# TabooLib 启动逻辑

## 1. 启动 `IsolatedClassLoader`

由继承 `JavaPlugin` 的插件主类在 `static` 块中启动 `IsolatedClassLoader`（下文简称：**沙盒**）

```java
public class TestPlugin extends JavaPlugin {

    static {
        IsolatedClassLoader.init(TestPlugin.class);
    }
}
```

随后由 **沙盒** 加载 `PrimitiveLoader` 并启动。

```java
public class IsolatedClassLoader {
    
    // ...
    
    public static void init(Class<?> clazz) {
        // 初始化隔离类加载器
        INSTANCE = new IsolatedClassLoader(clazz);
        // 加载启动类
        try {
            Class<?> delegateClass = Class.forName("taboolib.common.PrimitiveLoader", true, INSTANCE);
            Object delegateObject = delegateClass.getConstructor().newInstance();
            delegateClass.getMethod("init").invoke(delegateObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    // ...
}
```

## 2. 进入原始启动阶段

原始启动阶段即下载完整模块过程。

1. 首先，由 `PrimitiveLoader` 下载 `jar-relocator` 核心重定向工具，并载入沙盒。
2. 其次，由 `PrimitiveLoader` 下载项目所需要的所有 **TabooLib** 模块，并借助 `jar-relocate` 重定向后载入沙盒。
3. 最后，由 `PrimitiveLoader` 调用 `TabooLib#init` 函数，进入正式启动阶段。

> 优先下载 `jar-relocator` 是先决条件，虽有沙盒环境但存在多种限制，因此 TabooLib 项目依旧保留了重定向传统。
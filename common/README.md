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
2. 其次，由 `PrimitiveLoader` 下载项目所需要的所有 **TabooLib** 模块，并借助 `jar-relocate` 重定向后加载。

> 优先下载 `jar-relocator` 是先决条件，虽有沙盒环境但存在多种限制，因此 TabooLib 项目依旧保留了重定向传统。

经过 `PrimitiveLoader` 下载的模块会执行 `extra.properties` 中指向的入口函数。

```properties
main=common.env.RuntimeEnv
main-method=init
```

```properties
main=common.inject.VisitorHandler,common.io.ProjectScannerKt
main-method=init
```

模块的下载存在必要的先后顺序:

1. `common-env` 模块优先加载，用于启动 **Kotlin** 环境。
2. `common-util` 模块在 **Kotlin** 环境就绪后加载，注册 `ClassAppender Callback` 回调函数。

> 后续模块的加载无先后顺序。

## 3. 进入正式启动阶段

由继承 `JavaPlugin` 的插件主类在 `static` 块中运行 `TabooLib#lifeCycle` 生命周期函数。

```java
static {
    // ...
    TabooLib.lifeCycle(LifeCycle.CONST);
}
```

此时 **TabooLib** 基本加载完成，但依旧需要在 `onLoad`, `onEnable`, `onDisable` 等函数中运行生命周期函数。

## 总结

相比 `6.0` 版本，虽然解体后的 **TabooLib** 在加载逻辑上稍显复杂，但 `common` 模块在经历重构后变得更加清晰。

> `jar-relocator` -> `env` -> `kotlin` -> `ALL`
# TFunction
懒癌第二步，省略初始化方法  
当插件载入时 TabooLib 会为你 **自动执行** 初始化方法

## 使用环境
人类的本质是... （自动化接口，能省则省就对了）

## 基本用法
```java
@TFunction
public class Handler {

    static void onEnable() {
        // ... 初始化内容 
    }
    
    static void onDisable() {
        // ... 注销内容
    }
}
```

## 特殊参数
```java
@TFunction(
    // 加载时调用类中同名无参方法
    enable = "init",
    // 卸载时...
    disable = "cancel"
)
public class Handler {

    static void init() {
        // ... 初始化内容 
    }
    
    static void cancel() {
        // ... 注销内容
    }
}
```

## 易错问题
人类的本质是... (与 @TListener 相同）

<br>

# TFunction的子注解
和TFunction差不多，只是这些子注解在方法上。
+ @TFunction.Cancel
+ @TFunction.Init
+ @TFunction.Load

## 使用环境
```kotlin
object test {
    @TFunction.Init
    fun init() {
        println("我在Enable时调用")
    }

    @TFunction.Cancel
    fun cancel() {
        println("我在Disable时调用")
    }

    @TFunction.Load
    fun load() {
        println("我在Load时调用")
    }
}
```
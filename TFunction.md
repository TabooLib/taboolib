# TFunction
懒癌第二步，省略初始化方法  
当插件载入时 TabooLib 会为你 **自动执行** 初始化方法

## 使用环境
在我为我的服务器开发核心插件时，会出现这么一个问题。  
独立模块很多，非常多，我又不想给他们写到一起，那样会很乱。  
分开写又要一条一条执行初始化方法，太麻烦了！

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

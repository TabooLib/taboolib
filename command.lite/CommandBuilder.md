# 指令创建
> 简易指令创建工具

## 开始
我相信绝大多数开发者而不愿适用别人的格式编写自己指令  
试试这个吧，我也经常用

> 该工具可使用 @TInject 注解自动注册

## 使用
```java
    @TInject
    static CommandBuilder ping = CommandBuilder.create("ping", null)
            .execute((sender, args) -> {
                sender.sendMessage("pong!");
            });
```

> 上面这段代码可以写在主类以外的任何地方

## 参数
在 ``CommandBuilder`` 的创建过程中还可以带入一些其他参数
```java
    @TInject
    static CommandBuilder ping = CommandBuilder.create("ping", null)
            // 强制注册
            // 覆盖名称重复的其他指令
            .forceRegister()
            // 指令权限
            .permission("*")
            // 指令别名
            .aliases("p")
            // 指令补全
            .tab((sender, args) -> {
                return Lists.newArrayList();
            })
            .execute((sender, args) -> {
                sender.sendMessage("pong!");
            });
```

> 部分不常用的参数就不重复列举了

# 标准指令（注册）
> 坏黑开发插件所使用的标准指令工具中的注册方式

0. 作用
用于将 ``BaseSubCommand`` 注册到服务器中

1. 使用
将注解 ``@BaseCommand`` 添加至标准指令类
```java
@BaseCommand(
        name = "exampleCommand", aliases = {"example"}, permission = "*"
)
public class ExampleCommand extends BaseMainCommand {

    @Override
    public String getCommandTitle() {
        return "§e§l----- §6§lExample Commands §e§l-----";
    }
}
```

2. 参数
在 BaseCommand 中还提供了其他相关功能
```java
public @interface BaseCommand {

    /**
     * 指令名称
     */
    String name();

    /**
     * 指令权限
     */
    String permission() default "";

    /**
     * 指令权限提示
     */
    String permissionMessage() default "";

    /**
     * 指令描述
     */
    String description() default "";

    /**
     * 指令用法
     */
    String usage() default "";

    /**
     * 指令别名
     */
    String[] aliases() default {};
}
```

> 使用 @BaseCommand 注册的指令会强制覆盖已经存在的重复指令

# 标准指令（注册）
> 坏黑开发插件所使用的标准指令工具中的注册方式

## 作用
用于将 ``BaseSubCommand`` 所实现的指令注册到服务器中

## 使用
将注解 ``@BaseCommand`` 添加至标准指令类
```java
@BaseCommand(name = "exampleCommand", aliases = {"example"}, permission = "*")
public class ExampleCommand extends BaseMainCommand {
    BaseSubCommand ping = new BaseSubCommand() {
        @Override
        public String getDescription() {
            return "砰!";
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String s, String[] args) {
            sender.sendMessage("pong!");
        }
    };
}
```

## 参数
在 ``@BaseCommand`` 中还提供了其他相关功能
```java
public @interface BaseCommand {

    /**
     * @return 命令名称，默认启用强制注册，替换冲突命令
     */
    String name();

    /**
     * @return 命令权限
     */
    String permission() default "";

    /**
     * @return 缺少权限提示
     */
    String permissionMessage() default "";

    /**
     * @return 默认权限设置
     */
    PermissionDefault permissionDefault() default PermissionDefault.OP;

    /**
     * @return 别名，别名不会被强制注册
     */
    String[] aliases() default {};

    /**
     * @return Bukkit 命令描述，不会在 BaseMainCommand 构建的帮助列表中显示
     */
    String description() default "";

    /**
     * @return Bukkit 使用方法，不会在 BaseMainCommand 构建的帮助列表中显示
     */
    String usage() default "";

}
```

> 使用 @BaseCommand 注册的指令会强制覆盖已经存在的重复指令

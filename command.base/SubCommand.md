# 标准指令（子命令注册）
> 坏黑开发插件所使用的标准指令工具中的子命令注册方式

## 作用
用于将 ``BaseSubCommand`` 所实现的子命令注册到主命令中

## 使用
将注解 ``@SubCommand`` 添加至标准指令（子命令）上

```java
@BaseCommand(name = "exampleCommand", aliases = {"example"}, permission = "*")
public class ExampleCommand extends BaseMainCommand {
    // 在注解中注册子命令描述，而省略覆写getDescription()方法
    @SubCommand(description = "砰!")
    BaseSubCommand ping = new BaseSubCommand() {
        @Override
        public void onCommand(CommandSender sender, Command command, String s, String[] args) {
            sender.sendMessage("pong!");
        }
    };
}
```

## 默认效果

![](https://i.loli.net/2021/06/20/LMWY8Jak64ABs2G.png)

## 参数
在 ``@SubCommand`` 中还提供了其他相关功能

```java
public @interface SubCommand {

    /**
     * @return 优先级，用于在帮助列表中排序子命令
     */
    double priority() default 0;

    /**
     * @return 子命令权限，缺少权限将不会显示在帮助列表中且无法执行
     */
    String permission() default "";

    /**
     * @return 描述，用于在帮助列表中显示
     */
    String description() default "";

    /**
     * @return 别名
     */
    String[] aliases() default {};

    /**
     * 用法：
     * {"player", "name?"}
     * 使用 "?" 结尾与 optional() 等价
     *
     * @return 参数
     */
    String[] arguments() default {};

    /**
     * @return 是否在帮助列表中隐藏该子命令
     */
    boolean hideInHelp() default false;

    /**
     * @return 指令执行者约束，类型无效将不会显示在帮助列表中且无法执行
     */
    CommandType type() default CommandType.ALL;
}
```

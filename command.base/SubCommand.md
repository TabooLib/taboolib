# 标准指令（子命令注册）
> 坏黑开发插件所使用的标准指令工具中的子命令注册方式

## 0. 作用
用于将 ``BaseSubCommand`` 所实现的子命令注册到主命令中

## 1. 使用
将注解 ``@SubCommand`` 添加至标准指令（子命令）上

```java
public class ExampleCommand extends BaseMainCommand {

    @Override
    public String getCommandTitle() {
        return "§e§l----- §6§lExample Commands §e§l-----";
    }

    @SubCommand
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

## 3. 参数
在 ``@SubCommand`` 中还提供了其他相关功能

```java
public @interface SubCommand {

    /**
     * 优先级
     * 子命令在指令帮助列表中的显示顺序
     */
    double priority() default 0;

}
```

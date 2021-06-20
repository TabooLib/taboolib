# 标准指令（子命令参数）
> 坏黑开发插件所使用的标准指令工具中的子命令参数

## 作用
用于在 ``BaseSubCommand`` 中实现子命令参数，提供了参数补全等功能

## 使用
```java
@BaseCommand(name = "exampleCommand", aliases = {"example"}, permission = "*")
public class ExampleCommand extends BaseMainCommand {
    @SubCommand
    BaseSubCommand ping = new BaseSubCommand() {

        @Override
        public String getDescription() {
            return "砰!";
        }
        
        @Override
        public Argument[] getArguments() {
            return new Argument[] { new Argument("ohh", true) };
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String s, String[] args) {
            // 注意 args[0] 为参数 "ohh" 的值，而不是 "ping"。
            sender.sendMessage("pong: " + args[0]);
        }
    };
}
```

## 参数
在 ``Argument`` 中提供了多种构造函数，他们分别代表不同的作用
```java
new Argument("玩家");
```
> 参数 "玩家"，不可省略

```java
new Argument("玩家", false);
```
> 参数 "玩家"，可以省略

```java
new Argument("玩家", true, () -> Lists.newArrayList("BlackSKY"));
```
> 参数 "玩家"，不可省略，参数补全候选为 "BlackSKY"。

## 实例
```java
@SubCommand
BaseSubCommand reload = new BaseSubCommand() {

    @Override
    public Argument[] getArguments() {
        return new Argument[] {
                new Argument("服务", () -> Lists.newArrayList(Cronus.getCronusService().getServices().keySet()))
        };
    }
...
```

> 以上代码片段来自开源项目 Cronus 的指令部分

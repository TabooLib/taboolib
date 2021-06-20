# 标准指令（子命令）
> 坏黑开发插件所使用的标准指令工具中的子命令实现

## 作用
用于在 ``BaseSubCommand`` 中实现子命令

## 使用
在标准命令类中创建 ``BaseSubCommand`` 成员变量
```java
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

> 你可以在 TabooLib 语言文件中完全自定义这套指令工具的提示信息

## 函数
在 ``BaseSubCommand`` 中还提供了其他相关功能

```java
BaseSubCommand ping = new BaseSubCommand() {

       /**
        * 子命令别名
        */
        @Override
        public String[] getAliases() {
            return new String[] { "p" };
        }

       /**
        * 子命令描述
        */
        @Override
        public String getDescription() {
            return "砰!";
        }
        
       /**
        * 子命令使用对象
        */
        @Override
        public CommandType getType() {
            return CommandType.PLAYER;
        }
        
       /**
        * 子命令使用权限
        * 没有权限时不会再指令帮助中列出
        */
        @Override
        public String getPermission() {
            return "*";
        }
        
       /**
        * 子命令是否在指令帮助中列出
        */
        @Override
        public boolean hideInHelp() {
            return true;
        }
        
       /**
        * 子命令参数
        */
        @Override
        public Argument[] getArguments() {
            return new Argument[0];
        }

        @Override
        public void onCommand(CommandSender sender, Command command, String s, String[] args) {
            sender.sendMessage("pong!");
        }
    };
```

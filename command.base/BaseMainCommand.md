# 标准指令
> 坏黑开发插件所使用的标准指令工具

## 0. 作用

高效的指令开发工具，包含指令补全、参数补全、相似匹配等功能

## 1. 使用

创建指令类并继承 ``BaseMainCommand`` 抽象类

```java
public class ExampleCommand extends BaseMainCommand {

    @Override
    public String getCommandTitle() {
        return "§e§l----- §6§lExample Commands §e§l-----";
    }
}
```

> 你可以在 TabooLib 语言文件中完全自定义这套指令工具的提示信息

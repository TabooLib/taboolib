# 标准指令
> 坏黑开发插件所使用的标准指令工具

## 作用

高效的指令开发工具，包含指令补全、参数补全、相似匹配等功能

## 使用

创建指令类并继承 ``BaseMainCommand`` 抽象类

```java
public class ExampleCommand extends BaseMainCommand {
    
}
```

> 你可以在 TabooLib 语言文件中完全自定义这套指令工具的提示信息

```yaml
#!/TabooLib/lang/zh_CN.yml
COMMANDS:
  INTERNAL:
    COMMAND-CREATE-FAILED: '&c插件 &7{0} &c的 &7{1} &c命令注册失败: &7{2}'
    TYPE-ERROR: '&8[&3&l{2}&8] &7指令 &f{0} &7只能由 &f{1} &7执行'
    TYPE-PLAYER: '玩家'
    TYPE-CONSOLE: '控制台'
  PARAMETER:
    UNKNOWN: '&8[&3&lTabooLib&8] &4b指令错误'
    INSUFFICIENT: '&8[&3&lTabooLib&8] &4参数不足'
  DISPLAY:
    CLASSIC:
      HELP: '§f/{0} {1} {2} §6- §e{3}'
      HELP-EMPTY: '§f/{0} {1} {2}'
      ARGUMENT-REQUIRED: '§7[§8{0}§7]'
      ARGUMENT-OPTIONAL: '§7<§8{0}§7>'
      ERROR-USAGE:
        - '&8[&3&l{2}&8] &7指令 &f{0} &7参数不足'
        - '&8[&3&l{2}&8] &7正确用法:'
        - '&8[&3&l{2}&8] &7{1}'
      ERROR-COMMAND:
        - '&8[&3&l{2}&8] &7指令 &f{0} &7不存在'
        - '&8[&3&l{2}&8] &7你可能想要:'
        - '&8[&3&l{2}&8] &7{1}'
    FLAT:
      HELP: '§f/{0} {1} {2} §8- §7{3}'
      HELP-EMPTY: '§f/{0} {1} {2}'
      ARGUMENT-REQUIRED: '§7[§8{0}§7]'
      ARGUMENT-OPTIONAL: '§7<§8{0}§7>'
      ERROR-USAGE:
        - '&8[&3&l{2}&8] &7指令 &f{0} &7参数不足'
        - '&8[&3&l{2}&8] &7正确用法:'
        - '&8[&3&l{2}&8] &7{1}'
      ERROR-COMMAND:
        - '&8[&3&l{2}&8] &7指令 &f{0} &7不存在'
        - '&8[&3&l{2}&8] &7你可能想要:'
        - '&8[&3&l{2}&8] &7{1}'
      HEAD:
        - ' '
        - ' &f&l{0}&7 v{1}'
        - ' '
        - ' &7命令: &f/{2} &8[...]'
        - ' &7参数:'
      BOTTOM:
        - ' '
      PARAMETERS:
        ==: JSON
        text:
          - '   &8- &f<{0}@0>'
          - '   &8  &7{1}'
        args:
          0:
            hover: '{2}'
            suggest: '{3}'
      PARAMETERS-EMPTY:
        ==: JSON
        text:
          - '   &8- &f<{0}@0>'
        args:
          0:
            hover: '{2}'
            suggest: '{3}'
```

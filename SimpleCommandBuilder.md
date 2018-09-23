# SimpleCommandBuilder
造指令的低级轮子，节省开发时间的

## 示范
简单的一批，根本不用多讲，不需要在 `plugin.yml` 里写任何东西，随时创建命令。
```java
public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // 创建命令
        SimpleCommandBuilder.create("test", this)
            .description("指令描述")
            .execute((sender, args) -> {
                // ...
                sender.sendMessage("指令执行成功");
                return true;
            }).build();
    }
}
```

## 方法
| 方法 | 作用 |
| --- | --- |
| description | 设置描述 |
| usage | 设置用法 |
| aliases | 添加别名 |
| permission | 设置权限 |
| permissionMessage | 设置权限提示 |
| execute | 设置指令执行器 |
| tab | 设置补全执行器 |

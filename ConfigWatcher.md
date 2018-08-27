# 文件监控
监听文件的更改，配置文件的超级助手

## 创建一个简单的文件监听
```java
public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // 获取文件
        File configFile = new File(getDataFolder(), "config.yml");
        // 创建监听
        TLib.getTLib().getConfigWatcher().addSimpleListener(configFile, new Runnable() {
            @Override
            public void run() {
                // 重载配置
                reloadConfig(); 
            }
        });
    }
}
```

## 注销文件监听
```java
public class Plugin extends JavaPlugin {

    @Override
    public void onDisable() {
        // 获取文件
        File configFile = new File(getDataFolder(), "config.yml");
        // 创建监听
        TLib.getTLib().getConfigWatcher().removeListener(configFile);
    }
}
```

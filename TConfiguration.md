# TConfiguration
懒癌第三步，省略配置重载  
基于 ConfigWatcher 的配置文件类型，自动重载。

## 使用环境
人类的本质是... （自动化接口，能省则省就对了）

## 基本用法
```java
public class Plugin extends JavaPlugin {

    private static TConfiguration conf;
  
    @Override
    public void onEnable() {
        // 创建配置
        conf = TConfiguration.createInResource(this, "config.yml");
        // 创建配置重载监听
        conf.listener(() -> getLogger().info("配置已重载")).runListener();
    }
}
```
!> 创建监听后不要忘记执行 runListener()。

## 灵魂用法
```java
public class Plugin extends JavaPlugin {

    @TInject("config.yml")
    private static TConfiguration conf;
  
    // ... 随便搞就行了
}
```
!> 注入会在 onEnable() 方法执行之前进行。

## 其他方法
| 方法 | 作用 |
| --- | --- |
| public String getStringColored(String path) | 获取文本并自动转换颜色 |
| public String getStringColored(String path, String def) | 获取文本并自动转换颜色（可定义默认值） |
| public List\<String\> getStringListColored(String path) | 获取文本集合并自动转换颜色 |
| public void release() | 释放文件监听 |
| public void reload() | 重新读取配置（会执行 runListener() 方法） |

# TValue
配置文件值注入工具

## 使用环境

一般情况下，我们获取配置文件的一个键的值是这样做的
```java
public class Plugin extends JavaPlugin {
    @Override
    public void onEnable(){
        String value = getConfig().get("Test.test");
    }
}

```
这样做看起来很不好看，还可以用`TConfig`，就像这样
```java
public class Plugin extends Plugin {
    @TInject("config.yml")
    static TConfig conf;

    @Override
    public void onEnable(){
        String value = conf.get("Test.test");
    }
}
```

还不够! 有了`TValue`后我们就可以这样做:
```java
public class Plugin extends Plugin {
    @TValue(value = "config.yml", node = "Test.test")
    static Object value;
}
```
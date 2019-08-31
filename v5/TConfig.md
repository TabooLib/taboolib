# 配置文件
> 自动释放 & 加载 & 重载的配置文件

## 0. 作用
使用该工具跳过配置文件的各种繁琐步骤，以及独特的自动重载

> 该工具可使用 @TInject 注解自动注册

## 1. 使用
```java
@TInject("config.yml")
static TConfig conf;
```

> 上面这段代码可以写在任何地方，读取方式与普通配置文件相同

## 2. 重载
```java
@TInject(value = "config.yml", reload = "reload")
static TConfig conf;

static void reload() {
    // reload logic
}
```

> 配置文件加载 & 重载时，执行 ``reload`` 参数所指向的静态方法

## 3. 函数
在 ``TConfig`` 中还提供了一些独特的方法
```java
    /**
     * 释放文件监听
     */
    public void release();

    /**
     * 重载配置
     */
    public void reload();

    /**
     * 写入文件
     */
    public void saveToFile();
    
    /**
     * 获取文件
     */
    public File getFile();
```

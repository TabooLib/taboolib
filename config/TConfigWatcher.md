# 文件监听
> 监听文件的修改

## 作用
当文件被修改时执行动作，为 ``TConfig`` 工具提供自动重载服务

## 注册
```java
    @Override
    public void onStarting() {
        TConfigWatcher.getInst().addSimpleListener(new File(getDataFolder(), "config.yml"), () -> {
            // reload logic
        });
    }
```

## 注销
```java
    @Override
    public void onStopping() {
        TConfigWatcher.getInst().removeListener(new File(getDataFolder(), "config.yml"));
    }
```

> 该工具因适用范围较小，所以并没有过多的简化调用方式

# 本地数据注解
> 本地文件数据托管系统生成注解

## 作用

用于声明本地数据文件，省去繁琐的赋值和获取哦步骤以及首次调用时的卡顿

## 使用

```java
@LocalFile("data.yml")
static FileConfiguration data;
```

该过程与以下步骤同理

```java
static FileConfiguration data;

@Override
public void onStarting() {
    data = Local.get().get("data.yml")
}
```

> 非主类成员需要标记为静态 `static` 类型

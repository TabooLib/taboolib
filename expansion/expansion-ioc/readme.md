
初始化IOC容器
```kotlin
@Awake(LifeCycle.INIT)
fun init() {
    //注册你的插件的包 这里是要扫描的包
    IOCReader.readRegister(runningClasses)
    //你可以选择使用你的Dao
    IOCReader.readRegister(runningClasses, IOCDaoYamlGson())
}
```
如果你想针对某个对象进行单独的Dao设计你也可以这样做

```kotlin
@SubscribeEvent
fun onBeanReadEvent(event: FieldReadEvent) {
    if (event.field.type == ArrayList::class.java){
        event.iocDao = IOCDaoSQL()
    }
}
```
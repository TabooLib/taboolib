
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

如何使用？

```kotlin

@Autowried
val data = ArrayList<YourData>()

```
注意: 你的数据类里要按照序列化规则进行书写

例如：IOCDatabaseYaml用的是 TabooLib的序列化规范

你的特殊类就要自定义处理

```kotlin

data class RegionData(
    var id: String? = null,
    @Conversion(LocationConverter::class)
    var one: Location? = null,
    @Conversion(LocationConverter::class)
    var two: Location? = null,
    var join: List<String>? = null,
    var leave: List<String>? = null,
    var weight: Int = 0
)

class LocationConverter : Converter<Location, String> {
    override fun convertToField(value: String): Location {
        val data = value.split(",")
        val world = Bukkit.getWorld(data.getOrElse(0) { "world" }) ?: Bukkit.getWorlds().first()!!
        return Location(
            world,
            data.getOrNull(1)?.toDoubleOrNull() ?: 0.0,
            data.getOrNull(2)?.toDoubleOrNull() ?: 0.0,
            data.getOrNull(3)?.toDoubleOrNull() ?: 0.0,
            data.getOrNull(4)?.toFloatOrNull() ?: 0F,
            data.getOrNull(5)?.toFloatOrNull() ?: 0F,
        )
    }

    override fun convertFromField(value: Location): String {
        return "${value.world!!.name},${value.x},${value.y},${value.z},${value.pitch},${value.yaw}"
    }
}
```

或者是使用IOCDatabaseYamlGson

```kotlin

你要先定义一下Gson的解析
IOCReader.readRegister(runningClasses, IOCDaoYamlGson().apply{
    gson = GsonBuilder().apply{
        //省略具体代码
    }.create()!!
})

```

你也可以自己定义一个数据储存
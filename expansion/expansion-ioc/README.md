### 初始化IOC容器

```kotlin
@Awake(LifeCycle.INIT)
fun init() {
    //注册你的插件的包 这里是要扫描的包
    //也可以用runningClassesWithoutLibrary
    //看需求选用
    IOCReader.readRegister(runningClasses)
    //你可以选择使用你的Database
    IOCReader.readRegister(runningClasses, 自定义的Database())
}
```

如果你想针对某个变量进行单独的Database设计你也可以这样做

```kotlin
@SubscribeEvent
fun onBeanReadEvent(event: FieldReadEvent) {
    if (event.field.name == "PlayerData") {
        event.iocDao = IOCDatabaseSQL()
    }
}
```

### 如何使用？

```kotlin

@Autowried
val data = ArrayList<YourData>()

```

### 自定义IOC

注意: 你的数据类里要按照序列化规则进行书写

例如：IOCDatabaseYaml用的是 Gson的序列化规范

你的特殊类就要自定义处理

```kotlin

@Component
data class RegionData(
    var id: String? = null,
    var one: Location? = null,
    var two: Location? = null,
    var join: List<String>? = null,
    var leave: List<String>? = null,
    var weight: Int = 0
)

```

### 自定义序列化反序列化

如果要修改序列化与反序列化的方式

```kotlin

//里面指定一下你要用的方式
@Component("Kotlinx")
data class RegionData(
    var id: String? = null,
    //...
)

```

同时提供了Event的方式进行修改

注释的优先级要大于Event

好处是这样你的这个Function不用注册（不过也没啥好处=-=）

```kotlin
class SerializationGetFunctionEvent(
    val data: Any,
    val targetFlag: String,
    var function: SerializeFunction,
) : ProxyEvent()
```

然后你要注册你的序列化反序列化方法

```kotlin
interface SerializeFunction {

    val name: String //Kotlinx 上文写的

    fun serialize(data: Any): String
    fun deserialize(data: Any, target: Class<*>, type: Type): Any?

}
```

然后注册即可

一定要在*CONST*的时候注册

```kotlin
@Awake(LifeCycle.CONST)
fun init() {
    val data = SerializationFunctionGson()
    SerializationManager.function[data.name] = data
}
```

### 注册一个IOC容器存储方式

```kotlin
interface IOCDatabase {

    fun init(clazz: Class<*>, source: String): IOCDatabase
    fun getDataAll(): Map<String, Any?>

    fun getData(key: String): Any?

    fun saveData(key: String, data: Any): Boolean

    fun saveDatabase()

    fun resetDatabase()

}
```

然后在你启动IOC容器的时候选择

```kotlin
IOCReader.readRegister(runningClasses, 自定义的Database())
```

当然也提供了另一种通过Event来选择的方式

```kotlin
class FieldReadEvent(
    val clazz: Class<*>,
    val field: Field,
    var iocDatabase: IOCDatabase
) : ProxyEvent()
```

说明：TypeReader负责的功能很多

这是因为我希望如果有个别需求要自定义TypeReader来满足个性化需求

要满足个性化需求又不改库就只能把这样的功能写紧凑

这样虽然不符合单一职责的原则但是方便了其他开发者进行拓展

### 容器选择器

目前支持了几个常用的容器

```kotlin
ArrayList
Collection
CopyOnWriteArrayList
MutableList

HashMap
MutableMap
ConcurrentHashMap
```

如果使用这几种容器就直接使用即可 

如果有其他选择就要注册自定义的容器

```kotlin
interface TypeRead {

    val type: Class<*>

    fun readAll(clazz: Class<*>, field: Field, database: IOCDatabase): Any

    fun writeAll(field: Field, source: Class<*>, database: IOCDatabase)

}
```

然后注册

一定要在*CONST*的时候注册
```kotlin

@Awake(LifeCycle.CONST)
fun init() {
   val list = TypeReaderArrayList()
   TypeReadManager.typeReader[list.type.name] = list
}
```

同时也提供了Event的方式进行选择

```kotlin
class GetTypeReaderEvent(
    val clazz: Class<*>,
    var reader: TypeRead? = null
) : ProxyEvent()
```

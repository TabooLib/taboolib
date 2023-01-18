# TabooIOC
#### Input and Output Container

帮助你管理数据类的工具

### 术语解释

Database: 负责数据实际存储 默认工具是IOCDatabaseYaml

IOCDataMap: 实际存储数据对象的位置 位于IOCReader.dataMap

Linker: 用于连接IOCDataMap的工具

Index：数据类的唯一键 确定了数据的唯一性

Serialization: 序列化反序列化的工具


### 运行流程

![img.png](img.png)

### Linker设计思路

![img_1.png](img_1.png)

### 初始化IOC管理器

```kotlin
@Awake(LifeCycle.INIT)
fun init() {
    // 注册你的插件的包 这里是要扫描的包
    // 也可以用runningClassesWithoutLibrary
    // 看需求选用
    IOCReader.readRegister(runningClasses)
    // 你可以选择使用你的Database
    IOCReader.readRegister(runningClasses, 自定义的Database())
}
```

如果你想针对某个变量进行单独的Database设计你也可以这样做

```kotlin
@SubscribeEvent
fun onBeanReadEvent(event: FieldReadEvent) {
    if (event.field.name == "PlayerData") {
        event.iocDatabase = IOCDatabaseSQL()
    }
}
```

### 如何使用？

可以使用以下三种方法声明你的数据Linker工具

三种方法都是连接IOC容器的数据

可以多次声明 同类型的Linker 同增减同修改

接下来你就可以放心的进行你的操作了 对象IO的事情就交给IOC进行管理吧

注: 这三个方法都是线程安全的底层实现是 ConcurrentHashMap

所以即使是List也没法get(index:Int) 但是提供了替代方法
```kotlin

var dataManager = linkedIOCMap<IOCData>()
var dataManager = linkedIOCList<IOCData>()
var dataManager = linkedIOCSingleton<IOCData>()

```

### 创建数据类

注意: 你的数据类里要按照序列化选择器规则进行书写

例如：IOCDatabaseYaml用的是 Gson的序列化规范

你的特殊类就要自定义处理

#### 注意
按照规范请必须声明你的index索引 保证数据唯一性

下面的示例代码中 id就是数据ID 且为string类型（支持基础类型）

推荐使用String作为数据ID

```kotlin

@Component(index = "id")
data class RegionData(
    var id: String? = null,
    var one: Location? = null,
    var two: Location? = null,
    var join: List<String>? = null,
    var leave: List<String>? = null,
    var weight: Int = 0
)

```
#### 单例模式

单例模式的ID将由IOC容器进行分配
```kotlin
@Component(singleton = true)
data class RegionData(
    var id: String? = null,
    var one: Location? = null,
    var two: Location? = null,
    var join: List<String>? = null,
    var leave: List<String>? = null,
    var weight: Int = 0,
)
```

#### 注释
```kotlin
@Retention(AnnotationRetention.RUNTIME)
annotation class Component(
    // 序列化工具选择器
    val function: String = "Gson",
    // 数据索引ID
    val index: String = "null",
    // 单例模式
    val singleton: Boolean = false,
)
```

### 自定义序列化反序列化

如果要修改序列化与反序列化的方式

```kotlin

// 里面指定一下你要用的方式
@Component("Kotlinx",index = "id")
data class RegionData(
    var id: String? = null,
    // ...
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

    val name: String // Kotlinx 上文写的

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

### 注册一个自定义Database

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
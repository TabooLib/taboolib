# TSchedule

快速创建任务调度

## 实例: 每分钟向控制台发送一条消息
```kotlin
@TSchedule(period = 60 * 20)
fun run() {
    Bukkit.getLogger().info("test")
}
```
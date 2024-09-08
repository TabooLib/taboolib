# expansion-submit-chain

对 Kotlin `Coroutine API` 的封装。

## 范例代码

```kotlin
submitChain {
    // 等待 10 ticks
    wait(10)
    // 同步执行代码
    sync {
        sender.sendMessage("Hello from Sync!")
    }
    wait(20)
    // 异步执行代码
    async {
        sender.sendMessage("Hello from Async!")
    }
    wait(5)
    // 允许求值
    val value = async {
        1 + 2 + 3
    }
    sync {
        sender.sendMessage("Value: $value")
    }
    wait(100)
    var index = 0
    // 重复执行的同步代码，每 20 ticks 执行一次
    val context = sync(period = 20L, delay = 0L) {
        index += 1
        sender.sendMessage("Sync: $index")
        if (index == 10) {
            sender.sendMessage("&cSync task cancelled.".colored())
            cancel()
            "END"
        } else {
            // 由于任务没有被 cancel，所以这里的返回值不会应用在 context 上
            "_"
        }
    }
    sync {
        sender.sendMessage("Sync: $context")
        // "Sync: END"
    }
}
```
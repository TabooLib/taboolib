`com.ilummc.tlib.util.Ref`

## getDeclaredFields

众所周知（~~啥你竟然不知道~~） `Class#getDeclaredFields` 方法在获取时，如果该类的某个字段的类不在 classpath 里面，那么就会抛出一个 `NoClassDefFoundError` 异常。

而在 Ref 类中的几个 getDeclaredFields 方法解决了这个问题：返回一个 `List<Field>` 实例，只包含在 classpath 中的类。

## getCallerClass

众所周知（~~啥你竟然不知道~~） Oracle 正在对内部包比如 `sun.misc` 这样的包动手脚，并且这些包可能在未来的版本里被删除（我们的 `Unsafe` 已经没了）。

而某个 Reflection 类就很好用，特别是 `Reflection#getCallerClass(int)` 这个方法。于是 Ref 中的 getCallerClass 方法解决了这个问题：通过分析栈调用来获取 caller class，并且在 Reflection 这个类还存在的时候用原来的方法调用。

在某个方法中调用 Ref.getCallerClass(3) 会返回调用这个方法的类。
`me.skymc.taboolib.string.ArrayUtils`

ArrayUtils 提供了一些关于数组的常用方法。

## arrayJoin

这个方法在使用指令的时候会经常用到，例如 `/say BlackSKY Hello World!`。  
我们需要获取这个命令第二个参数之后的所有内容才能保证发送信息的完整性。

```java
String message = ArrayUtils.arrayJoin(args, 1);
```

## asList

与 `Array.asList()` 的作用相同，唯一的区别在于允许对返回集合进行增删操作。

```java
List<String> list = ArrayUtils.asList("1", "2");
// Safely
list.add("3");
System.out.println(list);
```

## arrayAppend

用于将对象添加到数组末尾

```java
String[] array = new String[] {"1", "2"};
array = ArrayUtils.arrayAppend(array, "3");
```

## arrayAddFirst

用于将对象添加到数组开头

```java
String[] array = new String[] {"2", "3"};
array = ArrayUtils.arrayAddFirst(array, "1");
```

## arrayExpend

数组扩容（末尾追加）

```java
String[] array = new String[] {"1", "2"};
array = ArrayUtils.arrayExpend(array, 1);
array[2] = "3";
```

## arrayExpendAtFirst

数组扩容（开头插入）

```java
String[] array = new String[] {"2", "3"};
array = ArrayUtils.arrayExpendAtFirs(array, 1);
array[0] = "1";
```

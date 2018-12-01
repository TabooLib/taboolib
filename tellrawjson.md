# TellrawJson
便捷 Tellraw 信息发送工具

## 作用

TellrawJson 基于 `md_5` 的 `TextComponent` 相关工具。  
最大化的减少开发成本，简化了原版 Tellraw 信息的发送过程。

> TLocale 的 `BOOK` 功能就是由 TellrawJson 开发而成的。

## 开始

**创建对象**

```java
TellrawJson json = TellrawJson.create();
```

**添加文本**

相信用过原版 Tellraw（TextCompent） 发送方式的应该知道，Tellraw 信息是多个文本组合在一起发送的。  
在这里我们称作 `文本块`，每个文本块有着独立的动作，如 `hover`、`command` 等等。  

首先我们添加一个文本块：  

```java
TellrawJson json = TellrawJson.create()
    .append("Hello World!");
```

**添加动作**

我们可以通过以下几种方式来为刚才添加的文本块设定动作。  

```java
TellrawJson json = TellrawJson.create()
    .append("Hello World!")
    .hoverText("悬浮文本显示")
    .hoverItem(new ItemStack(Material.STONE)) // 基于 ViaVersion 和 ProtocolSupport 修复了 NBTTagList 的版本差异问题。
    .clickCommand("/say 点击执行指令")
    .clickSuggest("/say 点击建议指令")
    .clickOpenURL("https://www.mcbbs.net/");
```

!> `click` 与 `hover` 在每个文本块中只能各出现一次，Tellraw 信息是不允许又执行又建议指令的。

**发送文本**

```java
TellrawJson json = TellrawJson.create()
    .append("Hello World!")
    .hoverText("悬浮文本显示")
    .send(player);
```

是不是比 TextComponent 要简单的多了

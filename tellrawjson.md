`me.skymc.taboolib.json.tellraw.TellrawJson`

## 作用
---

TellrawJson 封装了 `md_5` 的 `TextComponent` 相关工具。  
最大化的减少开发成本，简化了原版 Tellraw 信息的发送过程。

> TLocale 的 `BOOK` 功能就是由 TellrawJson 开发而成的。

## 开始
---

**创建对象**

```java
TellrawJson json = TellrawJson.create();
```

**添加文本**

相信用过原版 Tellraw（TextCompent） 发送方式的应该知道，Tellraw 信息是多个文本组合在一起发送的。  
在这里我们称作 `文本块`，每个文本块有着独立的动作，如 `hover`、`command` 等等。  

首先我们添加一个文本块：  

```java
TellrawJson json = TellrawJson.create();
json.append("Hello World!");
```

**添加动作**

我们可以通过以下几种方式来为刚才添加的文本块设定动作。  

```java
TellrawJson json = TellrawJson.create();
json.append("Hello World!");
json.hoverText("悬浮文本显示"):
json.hoverItem(new ItemStack(Material.STONE));
json.clickCommand("/say 点击执行指令");
json.clickSuggest("/say 点击建议指令");
json.clickOpenURL("https://www.mcbbs.net/");
```

!> `click` 与 `hover` 在每个文本块中只能各出现一次，Tellraw 信息是不允许又执行又建议指令的。

**发送文本**

最激动人心的时刻到了?  

```java
TellrawJson json = TellrawJson.create();
json.append("Hello World!");
json.hoverText("悬浮文本显示"):
json.send(player);
```

是不是比 TextComponent 要简单的多了

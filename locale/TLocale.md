# TLocale

语言文件的重新定义

## 注册语言文件

TLocale 的语言文件是自动加载的，你可以通过以下任意一种方式来注册语言文件。

1. 在 `plugin.yml` 文件中添加 TabooLib 为 `depend` 或 `softdepend`。
2. 在插件主类添加 `@TLocalePlugin` 注解。

## 添加语言文件

语言文件应该放在 `/lang/xx_XX.yml` 中，TabooLib 默认会加载 `zh_CN` 和 `en_US` 的语言文件。

在添加了语言文件后，你就应该可以使用 `com.ilummc.tlib.resources.TLocale` 类的所有静态方法了。

在 `/TabooLib/config.yml` 中有 `LOCALE` 下的几个选项可以设置加载的语言和是否默认启用 PlaceholderAPI

## 发送一条消息

`/lang/zh_CN.yml`
```yaml
TEST: '{0} 加入服务器'
TITLE_TEST:
  ==: TITLE
  title: '&b{0} 加入了服务器'
  subtitle: '&a{0} 太强了以至于他还有 subtitle 显示'
```

代码部分
```java
package com.ilummc.tlib;

import io.izzel.taboolib.loader.Plugin;
import io.izzel.taboolib.module.inject.TListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.ilummc.tlib.resources.TLocale;

@TListener
public class ExampleMain extends Plugin implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        TLocale.sendTo(event.getPlayer(), "TEST", event.getPlayer().getName());
        TLocale.sendTo(event.getPlayer(), "TITLE_TEST", event.getPlayer().getName());
    }
}
```

## 语言文本格式

TLocale 的语言文本采用 yml 存储，每一个键对应一个或多个值、

以下的格式都是可以的：
```yaml
normal: '一条普通的测试信息'
multiline: |-
  多行的测试信息
  这是 YAML 表示带有换行符字符串的方法
typeMessage:
  ==: TITLE
  title: 这是带有类型的语言文本
  subtitle: 在下方你可以看到所有可用的类型
combinedTypeMessage:
  - ==: TEXT
    text:
      - '这也是一条普通的测试信息'
      - '但是他有很多行'
      - '还有可替换的文本 {0}'
  - ==: ACTION
    text: '一条 ActionBar 消息'
  - ==: JSON
    text:
      - '这条消息仍然可以使用 TLocale.sendTo(sender, "combinedTypeMessage") 来发送'
      - '玩家会收到一条聊天栏消息'
      - '一条 ActionBar 的消息'
      - '和一条 JSON 文本，比如<§a§n点击这里就会执行一条命令@cmd>'
    args:
      cmd:
        hover: |-
          命令是 /tp ~ ~50 ~
          这一条消息展示了 TLocale 的所有特性
        command: '/tp ~ ~50 ~'
  - '最后，这个消息也能混着普通字符串用'
```

TLocale 可以发送普通的聊天消息和其他复杂的消息，比如 ActionBar 和 Title 消息。

为了方便起见，我们将如下的配置
```yaml
==: 种类
参数1: xxxx
参数2: yyyy
```
称为一个 `语言对象`。

每个语言对象可以有以下几种排列方式：
```yaml
文本1: 语言对象
文本2:
  - 语言对象
  - 语言对象
节点1:
  文本3: 语言对象
```

TLocale 的文本都支持替换，如 `{N}` ，替换的文本是 `TLocale` 的方法调用时传入的对应字符串。

!> 因为写这个功能的人是程序员，所以 **N 从 0 开始**

以下内容列举了所有的语言对象种类

### TEXT

`TEXT` 类型代表一条普通的聊天栏文本，有以下几种表示方法：
```yaml
node1: '文本'
node2:
  - '文本'
  - '文本'
node3:
  ==: TEXT
  text: '文本'
node4:
  ==: TEXT
  text:
    - '文本'
    - 'Placeholder API 测试 %vault_eco_balance%'
  papi: true
```

你可以在任意一个 `TEXT` 中添加 `papi: true` 来启用对 PlaceholderAPI 的支持。  
默认值为 `/TaabooLib/config.yml` 中 `LOCALE.USE_PAPI` 所设置的值。

所有的选项都是**可选的** 。

### TITLE

`TITLE` 类型代表一条 title 消息，可以含有淡入淡出的时间选项。

所有的选项都是**可选的** 。

```yaml
node:
  ==: TITLE
  title: '显示在屏幕正中的 title 文本'
  subtitle: 'subtitle 文本'
  fadein: 10
  fadeout: 10
  stay: 20
  papi: false
```

### ACTION

`ACTION` 类型代表一条 ActionBar 消息。

所有的选项都是**可选的** 。

```yaml
node:
  ==: ACTION
  text: '&6ActionBar &e文本'
  papi: false
```

### JSON

`JSON` 类型代表一条可以点击和可以含有悬浮文字的消息。

消息的文本在 `text` 中指定。

可以点击、鼠标停留的文本需要使用 `<可选的文本@参数名称>` 来表示，然后在 `args` 参数中添加你指定的参数名称并添加点击和悬浮的文本。

全部 JSON 的任何地方都可以启用替换功能，包括内置的替换和 PAPI 。

所有的选项都是**可选的** 。

```yaml
node:
  ==: JSON
  text:
    - '<点击建议命令@test1>'
    - '<点击执行命令@test2>'
    - '<鼠标停留查看悬浮字@test3>'
    - '<@test4>'
    - '<又可以点击又可以悬浮的信息@combined>'
    - '替换测试1 {0}'
    - '<@test6>'
  papi: true
  args:
    test1:
      suggest: '/say 建议执行的命令'
    test2:
      command: '/say 点击直接执行的命令'
    test3:
      hover: |-
        &6鼠标悬浮显示的文字
        &9可以是单行，也可以是多行
    test4:
      text: '你可以在参数中指定显示的文本，比如这一条'
    test5:
      command: '/say Hello World.'
      hover: '点击说一句 &cHello World'
    test6:
      text: 'JSON 文本也有参数替换的功能，比如 {1}'
      hover: |-
        悬浮字中也可以替换 {1}
        甚至可以加入 PAPI 变量如 %player_name%
      command: '点击文本也可以替换，不做演示'
```

### BOOK

`BOOK` 类型代表含有可以点击和可以含有悬浮文字的书本内容。

消息的文本在 `pages` 中指定，其余使用方式与 `JSON` 类型相同。

所有的选项都是**可选的** 。

```yaml
node:
  ==: BOOK
  pages:
    1:
      - '书本第一页'
      - '<点击建议命令@test1>'
      - '<点击执行命令@test2>'
    2:
      - '书本第二页'
      - '变量用法与 JSON 相同'
  papi: true
  args:
    test1:
      suggest: '/say 建议执行的命令'
    test2:
      command: '/say 点击直接执行的命令'
```

### SOUND

`SOUND` 代表向玩家发送音效提示。

音效在 `sound` 中指定，格式为 `音效名-音调-音量`

所有的选项都是**可选的** 。

```yaml
noded
  ==: SOUND
  sound: 'ENTITY_PLAYER_LEVEL-1-1'
```


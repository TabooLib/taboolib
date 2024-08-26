![](https://wiki.ptms.ink/images/6/69/Taboolib-png-blue-v2.png)

## TabooLib Framework

[![](https://app.codacy.com/project/badge/Grade/3e9c747cd4aa484ab7cd74b7666c4c43)](https://www.codacy.com/gh/TabooLib/TabooLib/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=TabooLib/TabooLib&amp;utm_campaign=Badge_Grade)
[![](https://www.codefactor.io/repository/github/taboolib/taboolib/badge)](https://www.codefactor.io/repository/github/taboolib/taboolib)
![](https://img.shields.io/github/contributors/taboolib/taboolib)
![](https://img.shields.io/github/languages/code-size/taboolib/taboolib)

TabooLib 正在进行底层重构，在新版本发布后，您可以参考迁移文档从 `6.1` 版本升级。   
在此之前，您可以使用 `6.1.2-beta10` 在最高 `1.20.4` 版本下开发。

**这个版本主要有哪些改动？**

1. 更快的启动速度（包括依赖下载、类检索、类注入等）。
2. 优化大量工具的底层逻辑。
3. 规范项目结构。
4. 优化配套插件。
5. 优化 `application` 模块，以及支持在 IDEA 中直接运行。
6. 支持 `1.21`。
7. ...

**哪些 API 受到了破坏性的影响？**

1. 以 `ClassVisitor` 为主的类注入 API，所有方法均有改动。
2. 以 `ProjectScannerKt` 为主的类扫描 API。
   1. 所有顶层字段的 `Class` 类型变更为 `ReflexClass`。
   2. 移除顶层函数 `Class.getInstance(newInstance)`。
   3. 移除顶层函数 `checkPlatform(Class)`。
3. 移除 `@PlatformImplementation` 注解及相关 API。
   

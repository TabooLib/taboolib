# TabooLib (Bukkit) 更新手册

本文档将系统的介绍如何更新 TabooLib 的 Bukkit 支持版本，如果未来 **坏黑** 无法及时更新，可参照本文档。

## 1. 构建核心

更新 Bukkit 版本必须使用 `BuildTools.jar` 手动构建服务端核心，因为我们需要一些副产物。

```
java -jar BuildTools.jar --rev 1.20
```

在这之后，您将得到如下文件：

```
.
├── apache-maven-3.6.0
├── BuildData
│   ├── bin
│   └── mappings
│       ├── bukkit-1.20-cl.csrg       <--- 类命名映射表
│       ├── bukkit-1.20.at
│       ├── bukkit-1.20.exclude
│       └── package.srg
├── Bukkit
├── CraftBukkit
├── PortableGit
├── PortableGit-2.30.0-64-bit
├── Spigot
├── work
│   ├── decompile-c571a01f
│   ├── bukkit-c571a01f-members.csrg  <--- 类成员映射表
│   ├── mapped.c571a01f.jar           <--- 反混淆后的服务端 (net.minecraft.server)
│   ├── mapped.c571a01f.jar-cl
│   ├── mapped.c571a01f.jar-m
│   ├── minecraft_server.1.20.jar
│   ├── minecraft_server.1.20.txt
│   ├── server-1.20.jar
├── BuildTools.jar
├── BuildTools.log.txt
└── spigot-1.20.jar                   <--- 用来开服的服务端
```

## 2. 整理核心及映射表

TabooLib 所使用的核心是经过调整的，以方便开发者直接使用。在此之前整个过程是由 **坏黑** 手动完成的，但是现在您可以使用 `servergen` 工具。

```
java -jar servergen.jar <BuildTools Folder>
```

```
sky@localhost .tools % java -jar servergen.jar /Users/sky/Downloads/BuildTools
BuildTools Folder: /Users/sky/Downloads/BuildTools
Unzip: datafixerupper-4.0.26.jar
Unzip: spigot-1.20.jar
Unzip: spigot-1.20-R0.1-SNAPSHOT.jar
Server Jar: spigot-1.20.jar
Server Version: 1.20 (R0.1-SNAPSHOT)
Class Mapping File: bukkit-1.20-cl.csrg
Member Mapping File: bukkit-c571a01f-members.csrg
Mapping Hash: c571a01f
Mapping Jar: mapped.c571a01f.jar
Generate: output-1.20-R0.1-SNAPSHOT.jar
Generate: output-deobf-1.20-R0.1-SNAPSHOT.jar
Done! (15100ms)
```

经过短暂的等待后，您将得到如下文件：

```
.
├── .workspace
│   ├── bukkit-1.20-cl.csrg.zip
│   ├── bukkit-c571a01f-members.csrg.zip
│   ├── output-1.20-R0.1-SNAPSHOT.jar            <---> 它就是 ink.ptms.core:v12000:12000:universal
│   ├── output-1.20-R0.1-SNAPSHOT.min.jar        <---> 它就是 ink.ptms.core:v12000:12000-minimize:universal
│   ├── output-deobf-1.20-R0.1-SNAPSHOT.jar      <---> 它就是 ink.ptms.core:v12000:12000:mapped
│   └── output-deobf-1.20-R0.1-SNAPSHOT.min.jar  <---> 它就是 ink.ptms.core:v12000:12000-minimize:mapped 
├── apache-maven-3.6.0
└── BuildData
...
```

整理完成后并不会自动上传，需要您手动上传到您的仓库中。

在 `MappingFile` 类中，您可以看到从 `1.17` 开始的所有映射文件，每次版本更新，您都需要将新的映射文件添加到 `MappingFile` 中。

```kotlin
@RuntimeResources(
    ...
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.20-cl.csrg", // 如果你的文件有 .zip 后缀，写到这里时要摘掉
        hash = "1e2870b303f37a07709c2045b5db7e6c79e48acd", // 原始文件的 SHA1 值（压缩前的）
        zip = true,
        tag = "1.20:combined" // 表示这是一个类映射表
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-c571a01f-members.csrg",
        hash = "de0d266adbbff4f7ffe4dd44ed0e36f9205b31b1",
        zip = true,
        tag = "1.20:fields"  // 表示这是一个字段映射表
    )
)
class MappingFile(val combined: String, val fields: String)
```

## 3. 代码更新

每当 TabooLib 支持新版本时，以下代码必须做出相应的更改：

### 3.1. `taboolib/module/nms/MinecraftVersion.kt`

```kotlin
    ···

    /**
     * 当前所有受支持的版本
     */
    val supportedVersion = arrayOf(
        arrayOf("1.8", "1.8.3", "1.8.4", "1.8.5", "1.8.6", "1.8.7", "1.8.8", "1.8.9"), // 0
        arrayOf("1.9", "1.9.2", "1.9.4"), // 1
        arrayOf("1.10.2"), // 2
        arrayOf("1.11", "1.11.2"), // 3
        arrayOf("1.12", "1.12.1", "1.12.2"), // 4
        arrayOf("1.13", "1.13.1", "1.13.2"), // 5
        arrayOf("1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4"), // 6
        arrayOf("1.15", "1.15.1", "1.15.2"), // 7
        arrayOf("1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5"), // 8
        // universal >= 9
        arrayOf("1.17", "1.17.1"),
        arrayOf("1.18", "1.18.1", "1.18.2"), // 10
        arrayOf("1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4"), // 11
        arrayOf("1.20") // 12 <--- 新的版本号
    )

    /**
     * 老版本格式
     */
    val majorLegacy by unsafeLazy {
        when (major) {
            0 -> 10800
            1 -> 10900
            2 -> 11000
            3 -> 11100
            4 -> 11200
            5 -> 11300
            6 -> 11400
            7 -> 11500
            8 -> 11600
            9 -> 11700
            10 -> 11800
            11 -> 11900
            12 -> 12000 // <--- 新的版本号
            else -> 0
        } + minor
    }

    ···
```

### 3.2. `taboolib/library/xseries/*`

该目录下包含部分 [XSeries](https://github.com/CryptoMorin/XSeries) 工具，您需要将其更新到最新版本。

```
XTag                      -> XMaterialUtil
javax.annotation.Nonnull  -> org.jetbrains.annotations.NotNull
javax.annotation.Nullable -> org.jetbrains.annotations.Nullable
```

### 3.3. `taboolib/module/nms/ConnectionGetterImpl.kt`

根据版本获取玩家连接。

### 3.4. `taboolib/module/nms/MinecraftServerUtil.kt`

根据版本获取服务端实例。

### 3.5 `taboolib/module/nms/i18n/I18nCurrently.kt`

根据版本获取语言文件。

> https://launchermeta.mojang.com/mc/game/version_manifest.json -> [version] -> [assetIndex] -> [...]

```kotlin
    ...
    val locales = arrayOf(
            arrayOf("zh_cn", "047c10e1a6ec7f7bcbb4d5c23a7d21f3b6673780"),
            arrayOf("zh_hk", "3bcb1edf75506bc790390ae1694db11334f77889"),
            arrayOf("zh_tw", "0eb2fb4d5c8cb3fe053589728140fe0d31f2edff"),
            arrayOf("en_gb", "d81bbc616f798828fdd41be3eb4c4a1d4ab6c168")
    )
    ...
```

## 4. 发布

默认情况下，TabooLib 在推送后会自动发布到 **坏黑** 的个人仓库供大家下载：

```
gradlew publish -Pbuild=BUILD_NUMBER
```

如果 **坏黑** 的个人仓库无法访问，便需要您在 `build.gradle.kts` 中更改发布地址：

```kotlin
fun PublishingExtension.applyToSub(subProject: Project) {
    repositories {
        maven("your repository") {
            isAllowInsecureProtocol = true
            credentials {
                username = project.findProperty("taboolibUsername").toString()
                password = project.findProperty("taboolibPassword").toString()
            }
            ...
```

如果您的仓库设置了访问权限，需要在 `~/User/用户/.gradle/gradle.properties` 中添加用户名和密码：

```
taboolibUsername=your repository username
taboolibPassword=your repository password
```

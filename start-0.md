## 下载 TabooLib SDK

在第五代版本的更新中，我们改变了书库原有的加载和使用方式  
导致在简化用户安装步骤的同时，导致开发者的环境搭建变得更加繁琐  

使用 TabooLib SDK 来跳过项目的准备工作

+ [TabooLibExample-maven-5.05](https://skymc.oss-cn-shanghai.aliyuncs.com/i/TabooLibExample-maven-5.05.zip)
+ TabooLibExample-gradle-5.05

> 在 TabooLib SDK 中内置了坏黑的高速远程仓库，包含所有版本的 Spigot 核心。

## Maven

```
    <repositories>
        <repository>
            <id>Purtmars Mirror-nms</id>
            <url>http://ptms.ink:8081/repository/codemc-nms/</url>
        </repository>
        <repository>
            <id>Purtmars Mirror</id>
            <url>http://ptms.ink:8081/repository/maven-releases/</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>io.izzel.taboolib</groupId>
            <artifactId>TabooLib</artifactId>
            <version>5.05</version>
            <classifier>all</classifier>
        </dependency>
        <dependency>
            <groupId>io.izzel.taboolib.loader</groupId>
            <artifactId>TabooLibloader</artifactId>
            <version>1.1</version>
            <classifier>all</classifier>
        </dependency>
    </dependencies>
```

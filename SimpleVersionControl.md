# SimpleVersionControl
基于 ASM 的自动 NMS 版本转换工具

## 示范
自动将 InternalPathfinderExecutor 类中所有 v1_8_R3 字段转换为当前服务器版本并跳过类初始化步骤。  
```java
public class SimpleAiSelector {

    private static PathfinderCreator internalPathfinderCreator;
    private static PathfinderExecutor internalPathfinderExecutor;

    public SimpleAiSelector() {
        try {
            internalPathfinderCreator = (PathfinderCreator) SimpleVersionControl.create()
                    .from("1_8_R3")
                    .target("me.skymc.taboolib.common.pathfinder.internal.InternalPathfinderCreator")
                    .translate()
                    .newInstance();
            internalPathfinderExecutor = (PathfinderExecutor) SimpleVersionControl.create()
                    .from("1_8_R3")
                    .target("me.skymc.taboolib.common.pathfinder.internal.InternalPathfinderExecutor")
                    .translate()
                    .newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
> 更多详细的用法可见本插件的 InternalPathfinderExecutor 和 InternalPathfinderCreator 类

## 1. 创建抽象类
本篇教程内容为使用 SimpleVersionControl 制作自动适配服务端版本的无反射 tps 获取工具。  
创建抽象类能够避免通过反射执行**生成类**（指本工具生成出来的类）中的方法，提升性能。
```java
package me.skymc.taboolib.example.tps;

public abstract class TPSTool {
    
    public abstract double[] getTPS();

}
```

## 2. 创建实现类
在实现类中你可以肆无忌惮的使用 nms 方法。
```java
package me.skymc.taboolib.example.tps;

import net.minecraft.server.v1_8_R3.MinecraftServer;

public class TPSTool extends AbstractTPSTool {

    @Override
    public double[] getTPS() {
        return MinecraftServer.getServer().recentTps;
    }
}
```

## 3. 转换
```java
package me.skymc.taboolib.example;

import me.skymc.taboolib.commands.builder.SimpleCommandBuilder;
import me.skymc.taboolib.common.versioncontrol.SimpleVersionControl;
import me.skymc.taboolib.example.tps.AbstractTPSTool;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
public class Main extends JavaPlugin {

    private AbstractTPSTool tpsTool;

    @Override
    public void onEnable() {
        try {
            tpsTool = (AbstractTPSTool) SimpleVersionControl.create()
                    // 插件
                    .plugin(this)
                    // 你在实现类中使用的 nms 版本
                    .from("1_8_R3")
                    // 实现类的地址
                    .target("me.skymc.taboolib.example.tps.TPSTool")
                    // 转换
                    .translate()
                    // 实例化
                    .newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 创建 getTPS 命令
        SimpleCommandBuilder.create("getTPS", this).execute((sender, args) -> {
            sender.sendMessage("tps: " + Arrays.toString(tpsTool.getTPS()));
            return true;
        }).build();
    }
}
```
> tps: [19.90449272934489, 19.980825292911394, 19.993604343142234]

## 已知问题
因个人 asm 水平有限，实现类中不可定义 nms 的变量，请用 Object 代替后强转：
```java
    public boolean navigationReach(LivingEntity entity) {
        Object pathEntity = this.getPathEntity(entity);
        return pathEntity == null || ((PathEntity)pathEntity).b();
    }
```

## 注意事项
1. 切勿忘记选择插件 `plugin(this)` 否则将无法获取实现类的文件位置。
2. 如果实现类没有继承 nms 的抽象类或接口，可用 `target(TPSTool.class)` 来选择实现类，否则必须使用文本路径来避免实现类被初始化。

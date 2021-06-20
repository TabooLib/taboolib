# 生物行为
> AI & Pathfinder

## 作用

生物行为是 minecraft 的内部系统，它控制生物的移动和目标等行为  
在没有高效工具的前提下创建或修改生物行为并不容易，需要读写大量混淆方法且版本兼容性较差  

这套工具可以使你在短时间内创建一套自己的生物行为  
本篇教程以 "**生物跟随**" 为例，演示本工具的使用方式  

## 行为方法

在一个标准的生物行为类中共有五个基本方法：

```java
public abstract class SimpleAi {

    public abstract boolean shouldExecute();

    public boolean continueExecute() {
        return shouldExecute();
    }

    public void startTask() {
    }

    public void resetTask() {
    }

    public void updateTask() {
    }
}
```

当生物被赋予该行为时，首先调用 ``shouldExecute`` 方法判断是否运行该行为  
如果返回真则立即调用则立刻调用 ``startTask`` 方法并在接下来的每刻中调用 ``updateTask`` 方法  
在 ``updateTask`` 执行之前会调用 ``continueExecute`` 方法判断是否继续运行该行为  
如果返回为假则调用 ``resetTask`` 方法并退出该行为  

## 创建行为

创建生物行为类并继承 ``SimpleAi`` 抽象类。

```java
public class FollowAi extends SimpleAi {

    // 目标对象
    private Player owner;
    // 生物对象
    private LivingEntity entity;
    // 移动速度
    private double speed;
    // 开始跟随距离
    private double startDistance = 5;
    // 停止跟随距离
    private double stopDistance = 2;
    // 传送距离
    private double teleportDistance = 10;

    public FollowAi(Player owner, LivingEntity entity, double speed) {
        this.owner = owner;
        this.entity = entity;
        this.speed = speed;
    }

    @Override
    public boolean shouldExecute() {
        return continueExecute();
    }

    @Override
    public boolean continueExecute() {
        return owner != null && owner.isOnline() && entity != null && entity.isValid();
    }

    @Override
    public void startTask() {
        updateTask();
    }

    @Override
    public void updateTask() {
        // 判断生物与目标是否在同一个世界或是进入传送距离
        if (!entity.getWorld().equals(owner.getWorld()) || entity.getLocation().distance(owner.getLocation()) > teleportDistance) {
            // 将生物传送至目标
            entity.teleport(owner.getLocation());
        // 判断生物与目标是否进入开始跟随距离
        } else if (entity.getLocation().distance(owner.getLocation()) > stopDistance) {
            // 调用寻路方法使生物移动至玩家位置
            SimpleAiSelector.getExecutor().navigationMove(entity, owner.getLocation(), speed);
        }
    }
}
```

## 赋予行为

首先打开 **EntityPig** 类来看一下它的原版行为：
```java
this.goalSelector.a(0, new PathfinderGoalFloat(this));
this.goalSelector.a(1, new PathfinderGoalPanic(this, 1.25D));
this.goalSelector.a(2, this.bm = new PathfinderGoalPassengerCarrotStick(this, 0.3F));
this.goalSelector.a(3, new PathfinderGoalBreed(this, 1.0D));
this.goalSelector.a(4, new PathfinderGoalTempt(this, 1.2D, Items.CARROT_ON_A_STICK, false));
this.goalSelector.a(4, new PathfinderGoalTempt(this, 1.2D, Items.CARROT, false));
this.goalSelector.a(5, new PathfinderGoalFollowParent(this, 1.1D));
this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
```

> 生物行为会按照优先级来运行，数值越小优先级越高。

```java
// 生成生物
LivingEntity entity = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), EntityType.PIG);
// 生物必须在完全生成后才可以被赋予行为
Bukkit.getScheduler().runTask(SpecialPet.getInst(), () -> {
    SimpleAiSelector.getExecutor().setGoalAi(entity, new FollowAi(player, entity, 1), 3);
});
```

在生物生成后，为该生物赋予优先级为 ``3`` 的跟随行为。

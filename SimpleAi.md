# SimpleAi
更简单的创建生物行为（Pathfinder）

## 什么是生物行为（Pathfinder）
生物行为是 mc 的内部系统，它控制生物的移动和目标等行为，原版的生物行为在 nms 包下以 PathfinderGoal 开头。  
如果想要完整使用这套内部系统便需要继承 nms 包下的 PathfinderGoal 类以及读写大量被混淆的方法名。  

在你需要频繁使用这套系统或是需要兼容多版本的时候，这么做是非常麻烦的。  
本工具提供了非常简单的生物行为创建方法，本章教程以 "**生物跟随**" 为例，演示本工具的使用方式。  

## 行为方法
在一个标准的生物行为类（SimpleAi）中共有五个基本方法：
+ boolean shouldExecute()
+ boolean continueExecute()
+ void startTask()
+ void updateTask()
+ void resetTask()

当生物被赋予该行为时，首先调用 **shouldExecute** 方法判断是否运行该行为  
如果返回真则立即调用则立刻调用 **startTask** 方法并在接下来的每刻中调用 **updateTask** 方法  
在 **updateTask** 执行之前会调用 **continueExecute** 方法判断是否继续运行该行为  
如果返回为假则调用 **resetTask** 方法并退出该行为  

## 创建行为
创建生物行为类并继承 SimpleAi 抽象类。
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
如果你正在编写的行为与移动有关，那么便离不开 **Navigation**，本工具提供了简单的寻路方法：
+ navigationMove(LivingEntity entity, Location location)
+ navigationMove(LivingEntity entity, Location location, double speed)
+ navigationReach(LivingEntity entity)

调用 **navigationMove** 会使你的生物按照原版寻路方式移动至目标位置  
调用 **navigationReach** 会返回生物是否已经抵达移动位置

## 赋予行为
当生物行为创建完成后，便可以使用以下方法在生物生成时赋予该行为。
```java
SimpleAiSelector.getExecutor().setGoalAi(LivingEntity entity, SimpleAi ai, int priority)
```

我们打开 **EntityPig** 来看一下它的原版行为：
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
生物行为会按照优先级来运行，数值越小优先级越高。

```java
LivingEntity entity = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), EntityType.PIG);
Bukkit.getScheduler().runTask(SpecialPet.getInst(), () -> SimpleAiSelector.getExecutor().setGoalAi(entity, new FollowAi(player, entity, 1), 3));
```
在生物生成后，为该生物赋予优先级为 `3` 的跟随行为。

!> 注意生物必须在完全生成后才可以赋予行为。

## PathfinderExecutor
你可以通过 **SimpleAiSelector** 调用该类来运行一些关于生物行为的方法

| 方法 | 作用 | 通过反射 |
| --- | --- | --- |
| Object getEntityInsentient(LivingEntity entity) | 获取 EntityInsentient 对象 | 否 |
| Object getNavigation(LivingEntity entity) | 获取 Navigation 对象 | 否 |
| Object getPathEntity(LivingEntity entity) | 获取 PathEntity 对象 | 是 |
| Object getControllerJump(LivingEntity entity) | 获取 ControllerJump 对象 | 否 |
| Object getControllerMove(LivingEntity entity) | 获取 ControllerMove 对象 | 否 |
| Object getControllerLook(LivingEntity entity) | 获取 ControllerLook 对象 | 否 |
| Object getGoalSelector(LivingEntity entity) | 获取 GoalSelector 对象 | 否 |
| Object getTargetSelector(LivingEntity entity) | 获取 TargetSelector 对象 | 否 |
| void setGoalAi(LivingEntity entity, SimpleAi ai, int priority) | 赋予移动行为 | 否 |
| void setTargetAi(LivingEntity entity, SimpleAi ai, int priority) | 赋予目标选择行为 | 否 |
| void clearGoalAi(LivingEntity entity) | 清理移动行为 | 是 |
| void clearTargetAi(LivingEntity entity) | 清理目标选择行为 | 是 |
| boolean navigationMove(LivingEntity entity, Location location) | 移动至目标位置 | 否 |
| boolean navigationMove(LivingEntity entity, Location location, double speed) | 移动至目标位置 | 否 |
| boolean navigationReach(LivingEntity entity) | 是否抵达目标位置 | 是 |
| void controllerLookAt(LivingEntity entity, Location target) | 看向目标位置 | 否 |
| void controllerLookAt(LivingEntity entity, Entity target) | 看向目标 | 否 |
| void controllerJumpReady(LivingEntity entity) | 使生物跳跃 | 否 |
| boolean controllerJumpCurrent(LivingEntity entity) | 生物正在跳跃 | 是 |







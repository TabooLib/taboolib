# 生物行为选择器
> 赋予行为或进行行为相关动作

## 获取

```java
PathfinderExecutor executor = SimpleAiSelector.getExecutor();
```

> 该工具类使用 SimpleVersionControl 进行版本控制

## 函数
```java
public abstract class PathfinderExecutor {

    /**
     * 将 LivingEntity 转换为 EntityInsentient
     */
    public abstract Object getEntityInsentient(LivingEntity entity);

    /**
     * 获取 Navigation
     */
    public abstract Object getNavigation(LivingEntity entity);

    /**
     * 获取 ControllerJump
     */
    public abstract Object getControllerJump(LivingEntity entity);

    /**
     * 获取 ControllerMove
     */
    public abstract Object getControllerMove(LivingEntity entity);

    /**
     * 获取 ControllerLook
     */
    public abstract Object getControllerLook(LivingEntity entity);

    /**
     * 获取 PathfinderGoalSelector
     */
    public abstract Object getGoalSelector(LivingEntity entity);

    /**
     * 获取 PathfinderTargetSelector
     */
    public abstract Object getTargetSelector(LivingEntity entity);

    /**
     * 获取 PathEntity
     */
    public abstract Object getPathEntity(LivingEntity entity);

    /**
     * 获取 PathEntity
     */
    public abstract void setPathEntity(LivingEntity entity, Object pathEntity);

    /**
     * 设置基本行为
     */
    public abstract void setGoalAi(LivingEntity entity, SimpleAi ai, int priority);

    /**
     * 设置目标选择
     */
    public abstract void setTargetAi(LivingEntity entity, SimpleAi ai, int priority);

    /**
     * 清除基本行为
     */
    public abstract void clearGoalAi(LivingEntity entity);
    
    /**
     * 清除目标选择
     */
    public abstract void clearTargetAi(LivingEntity entity);

    /**
     * 获取所有基本行为
     */
    public abstract Iterable getGoalAi(LivingEntity entity);

    /**
     * 获取所有目标选择
     */
    public abstract Iterable getTargetAi(LivingEntity entity);

    /**
     * 设置所有基本行为
     */
    public abstract void setGoalAi(LivingEntity entity, Iterable ai);

    /**
     * 设置所有目标选择
     */
    public abstract void setTargetAi(LivingEntity entity, Iterable ai);

    /**
     * 移动
     */
    public abstract boolean navigationMove(LivingEntity entity, Location location);

    /**
     * 移动（自定速度）
     */
    public abstract boolean navigationMove(LivingEntity entity, Location location, double speed);

    /**
     * 向实体移动
     */
    public abstract boolean navigationMove(LivingEntity entity, LivingEntity target);

    /**
     * 向实体移动（自定速度）
     */
    public abstract boolean navigationMove(LivingEntity entity, LivingEntity target, double speed);

    /**
     * 是否抵达目标
     */
    public abstract boolean navigationReach(LivingEntity entity);

    /**
     * 看向坐标
     */
    public abstract void controllerLookAt(LivingEntity entity, Location target);

    /**
     * 看向实体
     */
    public abstract void controllerLookAt(LivingEntity entity, Entity target);

    /**
     * 准备跳跃（执行该方法后生物将在短暂延迟后跳跃）
     */
    public abstract void controllerJumpReady(LivingEntity entity);

    /**
     * 是否准备跳跃
     */
    public abstract boolean controllerJumpCurrent(LivingEntity entity);

    /**
     * 获取跟随距离
     */
    public abstract void setFollowRange(LivingEntity entity, double value);
}
```

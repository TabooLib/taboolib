package taboolib.module.ai

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import taboolib.module.nms.nmsProxy

val pathfinderCreator = nmsProxy(PathfinderCreator::class.java)

val pathfinderExecutor = nmsProxy(PathfinderExecutor::class.java)

/**
 * 注册一个 Goal AI
 *
 * @param ai [SimpleAi]
 * @param priority 优先级
 */
fun LivingEntity.addGoalAi(ai: SimpleAi, priority: Int) {
    pathfinderExecutor.addGoalAi(this, ai, priority)
}

/**
 * 注册一个 Target AI
 *
 * @param ai [SimpleAi]
 * @param priority 优先级
 */
fun LivingEntity.addTargetAi(ai: SimpleAi, priority: Int) {
    pathfinderExecutor.addTargetAi(this, ai, priority)
}

/**
 * 根据优先级替换 Goal AI
 *
 * @param ai [SimpleAi]
 * @param priority 优先级
 */
fun LivingEntity.replaceGoalAi(ai: SimpleAi, priority: Int) {
    pathfinderExecutor.replaceGoalAi(this, ai, priority)
}

/**
 * 根据优先级替换 Target AI
 *
 * @param ai [SimpleAi]
 * @param priority 优先级
 */
fun LivingEntity.replaceTargetAi(ai: SimpleAi, priority: Int) {
    pathfinderExecutor.replaceTargetAi(this, ai, priority)
}

/**
 * 根据类名替换 Goal AI
 *
 * @param ai [SimpleAi]
 * @param priority 优先级
 * @param name 类名
 */
fun LivingEntity.replaceGoalAi(ai: SimpleAi, priority: Int, name: String?) {
    pathfinderExecutor.replaceGoalAi(this, ai, priority, name)
}

/**
 * 根据类名替换 Target AI
 *
 * @param ai [SimpleAi]
 * @param priority 优先级
 * @param name 类名
 */
fun LivingEntity.replaceTargetAi(ai: SimpleAi, priority: Int, name: String?) {
    pathfinderExecutor.replaceTargetAi(this, ai, priority, name)
}

/**
 * 根据优先级移除 Goal AI
 *
 * @param priority 优先级
 */
fun LivingEntity.removeGoalAi(priority: Int) {
    pathfinderExecutor.removeGoalAi(this, priority)
}

/**
 * 根据优先级移除 Target AI
 *
 * @param priority 优先级
 */
fun LivingEntity.removeTargetAi(priority: Int) {
    pathfinderExecutor.removeTargetAi(this, priority)
}

/**
 * 根据类名移除 Goal AI
 *
 * @param name 类名
 */
fun LivingEntity.removeGoalAi(name: String) {
    pathfinderExecutor.removeGoalAi(this, name)
}

/**
 * 根据类名移除 Target AI
 *
 * @param name 类名
 */
fun LivingEntity.removeTargetAi(name: String) {
    pathfinderExecutor.removeTargetAi(this, name)
}

/**
 * 清空所有 Goal AI
 */
fun LivingEntity.clearGoalAi() {
    pathfinderExecutor.clearGoalAi(this)
}

/**
 * 清空所有 Target AI
 */
fun LivingEntity.clearTargetAi() {
    pathfinderExecutor.clearTargetAi(this)
}

/**
 * 获取所有 Goal AI
 */
fun LivingEntity.getGoalAi(): Iterable<*> {
    return pathfinderExecutor.getGoalAi(this)
}

/**
 * 获取所有 Target AI
 */
fun LivingEntity.getTargetAi(): Iterable<*> {
    return pathfinderExecutor.getTargetAi(this)
}

/**
 * 设置所有 Goal AI
 *
 * @param ai [Iterable<*>]
 */
fun LivingEntity.setGoalAi(ai: Iterable<*>) {
    pathfinderExecutor.setGoalAi(this, ai)
}

/**
 * 设置所有 Target AI
 *
 * @param ai [Iterable<*>]
 */
fun LivingEntity.setTargetAi(ai: Iterable<*>) {
    pathfinderExecutor.setTargetAi(this, ai)
}

/**
 * 移动实体
 *
 * @param location 目标位置
 * @param speed 速度
 */
fun LivingEntity.navigationMove(location: Location, speed: Double = 0.2): Boolean {
    return pathfinderExecutor.navigationMove(this, location, speed)
}

/**
 * 移动实体
 *
 * @param target 目标实体
 * @param speed 速度
 */
fun LivingEntity.navigationMove(target: LivingEntity, speed: Double = 0.2): Boolean {
    return pathfinderExecutor.navigationMove(this, target, speed)
}

fun LivingEntity.navigationReach(): Boolean {
    return pathfinderExecutor.navigationReach(this)
}

/**
 * 看向目标
 *
 * @param target 目标位置
 */
fun LivingEntity.controllerLookAt(target: Location) {
    pathfinderExecutor.controllerLookAt(this, target)
}

/**
 * 看向目标
 *
 * @param target 目标实体
 */
fun LivingEntity.controllerLookAt(target: Entity) {
    pathfinderExecutor.controllerLookAt(this, target)
}

fun LivingEntity.controllerJumpReady() {
    pathfinderExecutor.controllerJumpReady(this)
}

fun LivingEntity.controllerJumpCurrent(): Boolean {
    return pathfinderExecutor.controllerJumpCurrent(this)
}
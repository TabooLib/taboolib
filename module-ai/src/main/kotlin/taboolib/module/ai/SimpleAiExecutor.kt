package taboolib.module.ai

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import taboolib.module.nms.nmsProxy

val pathfinderCreator = nmsProxy(PathfinderCreator::class.java)

val pathfinderExecutor = nmsProxy(PathfinderExecutor::class.java)

fun LivingEntity.addGoalAi(ai: SimpleAi, priority: Int) {
    pathfinderExecutor.addGoalAi(this, ai, priority)
}

fun LivingEntity.addTargetAi(ai: SimpleAi, priority: Int) {
    pathfinderExecutor.addTargetAi(this, ai, priority)
}

fun LivingEntity.replaceGoalAi(ai: SimpleAi, priority: Int) {
    pathfinderExecutor.replaceGoalAi(this, ai, priority)
}

fun LivingEntity.replaceTargetAi(ai: SimpleAi, priority: Int) {
    pathfinderExecutor.replaceTargetAi(this, ai, priority)
}

fun LivingEntity.replaceGoalAi(ai: SimpleAi, priority: Int, name: String?) {
    pathfinderExecutor.replaceGoalAi(this, ai, priority, name)
}

fun LivingEntity.replaceTargetAi(ai: SimpleAi, priority: Int, name: String?) {
    pathfinderExecutor.replaceTargetAi(this, ai, priority, name)
}

fun LivingEntity.removeGoalAi(priority: Int) {
    pathfinderExecutor.removeGoalAi(this, priority)
}

fun LivingEntity.removeTargetAi(priority: Int) {
    pathfinderExecutor.removeTargetAi(this, priority)
}

fun LivingEntity.removeGoalAi(name: String) {
    pathfinderExecutor.removeGoalAi(this, name)
}

fun LivingEntity.removeTargetAi(name: String) {
    pathfinderExecutor.removeTargetAi(this, name)
}

fun LivingEntity.clearGoalAi() {
    pathfinderExecutor.clearGoalAi(this)
}

fun LivingEntity.clearTargetAi() {
    pathfinderExecutor.clearTargetAi(this)
}

fun LivingEntity.getGoalAi(): Iterable<*> {
    return pathfinderExecutor.getGoalAi(this)
}

fun LivingEntity.getTargetAi(): Iterable<*> {
    return pathfinderExecutor.getTargetAi(this)
}

fun LivingEntity.setGoalAi(ai: Iterable<*>) {
    pathfinderExecutor.setGoalAi(this, ai)
}

fun LivingEntity.setTargetAi(ai: Iterable<*>) {
    pathfinderExecutor.setTargetAi(this, ai)
}

fun LivingEntity.navigationMove(location: Location, speed: Double = 0.2): Boolean {
    return pathfinderExecutor.navigationMove(this, location, speed)
}

fun LivingEntity.navigationMove(target: LivingEntity, speed: Double = 0.2): Boolean {
    return pathfinderExecutor.navigationMove(this, target, speed)
}

fun LivingEntity.navigationReach(): Boolean {
    return pathfinderExecutor.navigationReach(this)
}

fun LivingEntity.controllerLookAt(target: Location) {
    pathfinderExecutor.controllerLookAt(this, target)
}

fun LivingEntity.controllerLookAt(target: Entity) {
    pathfinderExecutor.controllerLookAt(this, target)
}

fun LivingEntity.controllerJumpReady() {
    pathfinderExecutor.controllerJumpReady(this)
}

fun LivingEntity.controllerJumpCurrent(): Boolean {
    return pathfinderExecutor.controllerJumpCurrent(this)
}
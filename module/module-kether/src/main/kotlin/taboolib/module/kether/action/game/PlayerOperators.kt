package taboolib.module.kether.action.game

import taboolib.common.platform.ProxyPlayer
import taboolib.module.kether.PlayerOperator

enum class PlayerOperators(
    val reader: ((ProxyPlayer) -> Any?)? = null,
    val writer: ((ProxyPlayer, PlayerOperator.Method, Any?) -> Unit)? = null,
    vararg val usable: PlayerOperator.Method,
) {

    LOCALE({ it.locale }),

    WORLD({ it.location.world }),

    X({ it.location.x }),

    Y({ it.location.y }),

    Z({ it.location.z }),

    YAW(
        { it.location.yaw },
        { p, m, v ->
            p.teleport(p.location.also {
                when (m) {
                    PlayerOperator.Method.INCREASE -> {
                        it.yaw += taboolib.common5.Coerce.toFloat(v)
                    }
                    PlayerOperator.Method.DECREASE -> {
                        it.yaw -= taboolib.common5.Coerce.toFloat(v)
                    }
                    PlayerOperator.Method.MODIFY -> {
                        it.yaw = taboolib.common5.Coerce.toFloat(v)
                    }
                    else -> {
                    }
                }
            })
        },
        *PlayerOperator.Method.values()
    ),

    PITCH(
        { it.location.pitch },
        { p, m, v ->
            p.teleport(p.location.also {
                when (m) {
                    PlayerOperator.Method.INCREASE -> {
                        it.pitch += taboolib.common5.Coerce.toFloat(v)
                    }
                    PlayerOperator.Method.DECREASE -> {
                        it.pitch -= taboolib.common5.Coerce.toFloat(v)
                    }
                    PlayerOperator.Method.MODIFY -> {
                        it.pitch = taboolib.common5.Coerce.toFloat(v)
                    }
                    else -> {
                    }
                }
            })
        },
        *PlayerOperator.Method.values()
    ),

    BLOCK_X({ it.location.blockX }),

    BLOCK_Y({ it.location.blockY }),

    BLOCK_Z({ it.location.blockZ }),

    COMPASS_X({ it.compassTarget.blockX }),

    COMPASS_Y({ it.compassTarget.blockY }),

    COMPASS_Z({ it.compassTarget.blockZ }),

    LOCATION(
        { taboolib.common.platform.function.platformLocation<Any>(it.location) },
        { p, _, v -> p.teleport(taboolib.common.platform.function.adaptLocation(v!!)) },
        PlayerOperator.Method.MODIFY
    ),

    COMPASS_TARGET(
        { taboolib.common.platform.function.platformLocation<Any>(it.compassTarget) },
        { p, _, v -> p.compassTarget = taboolib.common.platform.function.adaptLocation(v!!) },
        PlayerOperator.Method.MODIFY
    ),

    BED_SPAWN(
        { if (it.bedSpawnLocation != null) taboolib.common.platform.function.platformLocation<Any>(it.bedSpawnLocation!!) else null },
        { p, _, v -> p.bedSpawnLocation = if (v != null) taboolib.common.platform.function.adaptLocation(v) else null },
        PlayerOperator.Method.MODIFY
    ),

    BED_SPAWN_X({ it.bedSpawnLocation?.blockX }),

    BED_SPAWN_Y({ it.bedSpawnLocation?.blockY }),

    BED_SPAWN_Z({ it.bedSpawnLocation?.blockZ }),

    NAME({ it.name }),

    LIST_NAME(
        { it.playerListName },
        { p, _, v -> p.playerListName = v?.toString() },
        PlayerOperator.Method.MODIFY
    ),

    DISPLAY_NAME(
        { it.displayName },
        { p, _, v -> p.displayName = v?.toString() },
        PlayerOperator.Method.MODIFY
    ),

    UUID({ it.uniqueId.toString() }),

    GAMEMODE(
        { it.gameMode.name },
        { p, _, v ->
            p.gameMode = when (v.toString().uppercase(java.util.Locale.getDefault())) {
                "SURVIVAL", "0" -> taboolib.common.platform.ProxyGameMode.SURVIVAL
                "CREATIVE", "1" -> taboolib.common.platform.ProxyGameMode.CREATIVE
                "ADVENTURE", "2" -> taboolib.common.platform.ProxyGameMode.ADVENTURE
                "SPECTATOR", "3" -> taboolib.common.platform.ProxyGameMode.SPECTATOR
                else -> error("Unknown GameMode $v")
            }
        },
        PlayerOperator.Method.MODIFY
    ),

    ADDRESS({ it.address?.hostName }),

    SNEAKING({ it.isSneaking }),

    SPRINTING({ it.isSprinting }),

    BLOCKING({ it.isBlocking }),

    GLIDING(
        { it.isGliding },
        { p, _, v -> p.isGliding = taboolib.common5.Coerce.toBoolean(v) },
        PlayerOperator.Method.MODIFY
    ),

    GLOWING(
        { it.isGlowing },
        { p, _, v -> p.isGlowing = taboolib.common5.Coerce.toBoolean(v) },
        PlayerOperator.Method.MODIFY
    ),

    SWIMMING(
        { it.isSwimming },
        { p, _, v -> p.isSwimming = taboolib.common5.Coerce.toBoolean(v) },
        PlayerOperator.Method.MODIFY
    ),

    RIPTIDING({ it.isRiptiding }),

    SLEEPING({ it.isSleeping }),

    SLEEP_TICKS({ it.sleepTicks }),

    SLEEP_IGNORED(
        { it.isSleepingIgnored },
        { p, _, v -> p.isSleepingIgnored = taboolib.common5.Coerce.toBoolean(v) },
        PlayerOperator.Method.MODIFY
    ),

    DEAD({ it.isDead }),

    CONVERSING({ it.isConversing }),

    LEASHED({ it.isLeashed }),

    ON_GROUND({ it.isOnGround }),

    IS_ONLINE({ it.isOnline() }),

    INSIDE_VEHICLE({ it.isInsideVehicle }),

    OP(
        { it.isOp },
        { p, _, v -> p.isOp = taboolib.common5.Coerce.toBoolean(v) },
        PlayerOperator.Method.MODIFY
    ),

    GRAVITY(
        { it.hasGravity },
        { p, _, v -> p.hasGravity = taboolib.common5.Coerce.toBoolean(v) },
        PlayerOperator.Method.MODIFY
    ),

    ATTACK_COOLDOWN({ it.attackCooldown }),

    PLAYER_TIME(
        { it.playerTime },
        { p, m, v -> p.playerTime = p.playerTime.modify(m, v) },
        *PlayerOperator.Method.values()
    ),

    FIRST_PLAYED({ it.firstPlayed }),

    LAST_PLAYED({ it.lastPlayed }),

    ABSORPTION_AMOUNT(
        { it.absorptionAmount },
        { p, m, v -> p.absorptionAmount = p.absorptionAmount.modify(m, v) },
        *PlayerOperator.Method.values()
    ),

    NO_DAMAGE_TICKS(
        { it.noDamageTicks },
        { p, m, v -> p.noDamageTicks = p.noDamageTicks.modify(m, v) },
        *PlayerOperator.Method.values()
    ),

    REMAINING_AIR(
        { it.remainingAir },
        { p, m, v -> p.remainingAir = p.remainingAir.modify(m, v) },
        *PlayerOperator.Method.values()
    ),

    MAXIMUM_AIR({ it.maximumAir }),

    EXP(
        { it.exp },
        { p, m, v -> p.exp = p.exp.modify(m, v) },
        *PlayerOperator.Method.values()
    ),

    LEVEL(
        { it.level },
        { p, m, v -> p.level = p.level.modify(m, v) },
        *PlayerOperator.Method.values()
    ),

    EXHAUSTION(
        { it.exhaustion },
        { p, m, v -> p.exhaustion = p.exhaustion.modify(m, v) },
        *PlayerOperator.Method.values()
    ),

    SATURATION(
        { it.saturation },
        { p, m, v -> p.saturation = p.saturation.modify(m, v) },
        *PlayerOperator.Method.values()
    ),

    FOOD_LEVEL(
        { it.foodLevel },
        { p, m, v -> p.foodLevel = p.foodLevel.modify(m, v) },
        *PlayerOperator.Method.values()
    ),

    HEALTH(
        { it.health },
        { p, m, v -> p.health = p.health.modify(m, v) },
        *PlayerOperator.Method.values()
    ),

    MAX_HEALTH(
        { it.maxHealth },
        { p, m, v -> p.maxHealth = p.maxHealth.modify(m, v) },
        *PlayerOperator.Method.values()
    ),

    ALLOW_FLIGHT(
        { it.allowFlight },
        { p, _, v -> p.allowFlight = taboolib.common5.Coerce.toBoolean(v) },
        PlayerOperator.Method.MODIFY
    ),

    FLYING(
        { it.isFlying },
        { p, _, v -> p.isFlying = taboolib.common5.Coerce.toBoolean(v) },
        PlayerOperator.Method.MODIFY
    ),

    FLY_SPEED(
        { it.flySpeed },
        { p, m, v -> p.flySpeed = p.flySpeed.modify(m, v, max = 0.99f, min = -0.99f) },
        *PlayerOperator.Method.values()
    ),

    WALK_SPEED(
        { it.walkSpeed },
        { p, m, v -> p.walkSpeed = p.walkSpeed.modify(m, v, max = 0.99f, min = -0.99f) },
        *PlayerOperator.Method.values()
    ),

    PING({ it.ping }),

    POSE({ it.pose }),

    FACING({ it.facing });

    fun build() = PlayerOperator(
        reader = if (reader != null) PlayerOperator.Reader(reader) else null,
        writer = if (writer != null) PlayerOperator.Writer(writer) else null,
        usable = arrayOf(*usable)
    )
}
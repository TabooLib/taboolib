package taboolib.module.kether.action.game

import taboolib.common.platform.ProxyPlayer
import taboolib.module.kether.PlayerOperator

enum class PlayerOperators(
    val reader: ((ProxyPlayer) -> Any?)? = null,
    val writer: ((ProxyPlayer, PlayerOperator.Method, Any?) -> Unit)? = null,
    vararg val usable: PlayerOperator.Method,
) {

    LOCATION(
        { it.location },
        { p, _, v -> p.teleport(v as taboolib.common.util.Location) },
        PlayerOperator.Method.MODIFY
    ),

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

//    COMPASS_X(
//        PlayerOperator(
//            {
//                it.compassTarget.x
//            })
//    ),
//
//    COMPASS_Y(
//        PlayerOperator(
//            {
//                it.compassTarget.y
//            })
//    ),
//
//    COMPASS_Z(
//        PlayerOperator(
//            {
//                it.compassTarget.z
//            })
//    ),
//
//    COMPASS_TARGET(
//        PlayerOperator(
//            {
//                it.compassTarget
//            },
//            { p, _, v ->
//                p.compassTarget = v as org.bukkit.Location
//            }
//        )
//    ),
//
//    BED_SPAWN_X(
//        PlayerOperator(
//            {
//                it.bedSpawnLocation?.x
//            }
//        )
//    ),
//
//    BED_SPAWN_Y(
//        PlayerOperator(
//            {
//                it.bedSpawnLocation?.y
//            }
//        )
//    ),
//
//    BED_SPAWN_Z(
//        PlayerOperator(
//            {
//                it.bedSpawnLocation?.z
//            }
//        )
//    ),
//
//    BED_SPAWN(
//        PlayerOperator(
//            {
//                it.bedSpawnLocation
//            },
//            { p, _, v ->
//                p.bedSpawnLocation = v as org.bukkit.Location
//            }
//        )
//    ),
//
//    NAME(
//        PlayerOperator(
//            {
//                it.name
//            })
//    ),
//
//    DISPLAY_NAME(
//        PlayerOperator(
//            {
//                it.displayName
//            },
//            { p, _, v ->
//                p.setDisplayName(v.toString())
//            })
//    ),
//
//    LIST_NAME(
//        PlayerOperator(
//            {
//                it.playerListName
//            },
//            { p, _, v ->
//                p.setPlayerListName(v.toString())
//            })
//    ),
//
//    GAMEMODE(
//        PlayerOperator(
//            {
//                it.gameMode.name
//            },
//            { p, _, v ->
//                when (v.toString()) {
//                    "SURVIVAL", "0" -> p.gameMode = org.bukkit.GameMode.SURVIVAL
//                    "CREATIVE", "1" -> p.gameMode = org.bukkit.GameMode.CREATIVE
//                    "ADVENTURE", "2" -> p.gameMode = org.bukkit.GameMode.ADVENTURE
//                    "SPECTATOR", "3" -> p.gameMode = org.bukkit.GameMode.SPECTATOR
//                }
//            })
//    ),
//
//    ADDRESS(
//        PlayerOperator(
//            {
//                it.address?.hostName
//            }
//        )
//    ),
//
//    SNEAKING(
//        PlayerOperator(
//            {
//                it.isSneaking
//            }
//        )
//    ),
//
//    SPRINTING(
//        PlayerOperator(
//            {
//                it.isSprinting
//            }
//        )
//    ),
//
//    BLOCKING(
//        PlayerOperator(
//            {
//                it.isBlocking
//            }
//        )
//    ),
//
//    GLIDING(
//        PlayerOperator(
//            {
//                it.isGliding
//            },
//            { p, _, v ->
//                p.isGliding = io.izzel.taboolib.util.Coerce.toBoolean(v)
//            }
//        )
//    ),
//
//    GLOWING(
//        PlayerOperator(
//            {
//                it.isGlowing
//            },
//            { p, _, v ->
//                p.isGlowing = io.izzel.taboolib.util.Coerce.toBoolean(v)
//            }
//        )
//    ),
//
//    SWIMMING(
//        PlayerOperator(
//            {
//                it.isSwimming
//            },
//            { p, _, v ->
//                p.isSwimming = io.izzel.taboolib.util.Coerce.toBoolean(v)
//            }
//        )
//    ),
//
//    WHITELIST(
//        PlayerOperator(
//            {
//                it.isWhitelisted
//            },
//            { p, _, v ->
//                p.isWhitelisted = io.izzel.taboolib.util.Coerce.toBoolean(v)
//            }
//        )
//    ),
//
//    RIPTIDING(
//        PlayerOperator(
//            {
//                it.isRiptiding
//            }
//        )
//    ),
//
//    SLEEPING(
//        PlayerOperator(
//            {
//                it.isSleeping
//            }
//        )
//    ),
//
//    SLEEP_TICKS(
//        PlayerOperator(
//            {
//                it.sleepTicks
//            }
//        )
//    ),
//
//    SLEEP_IGNORED(
//        PlayerOperator(
//            {
//                it.isSleepingIgnored
//            },
//            { p, _, v ->
//                p.isSleepingIgnored = io.izzel.taboolib.util.Coerce.toBoolean(v)
//            }
//        )
//    ),
//
//    DEAD(
//        PlayerOperator(
//            {
//                it.isDead
//            }
//        )
//    ),
//
//    CONVERSING(
//        PlayerOperator(
//            {
//                it.isConversing
//            }
//        )
//    ),
//
//    LEASHED(
//        PlayerOperator(
//            {
//                it.isLeashed
//            }
//        )
//    ),
//
//    ON_GROUND(
//        PlayerOperator(
//            {
//                it.isOnGround
//            }
//        )
//    ),
//
//    INSIDE_VEHICLE(
//        PlayerOperator(
//            {
//                it.isInsideVehicle
//            }
//        )
//    ),
//
//    JUMPING(
//        PlayerOperator(
//            {
//                it.isJumping
//            },
//            { p, _, v ->
//                p.isJumping = io.izzel.taboolib.util.Coerce.toBoolean(v)
//            }
//        )
//    ),
//
//    OP(
//        PlayerOperator(
//            {
//                it.isOp
//            },
//            { p, _, v ->
//                p.isOp = io.izzel.taboolib.util.Coerce.toBoolean(v)
//            }
//        )
//    ),
//
//    GRAVITY(
//        PlayerOperator(
//            {
//                it.hasGravity()
//            },
//            { p, _, v ->
//                p.setGravity(io.izzel.taboolib.util.Coerce.toBoolean(v))
//            }
//        )
//    ),
//
//    ATTACK_COOLDOWN(
//        PlayerOperator(
//            {
//                it.attackCooldown
//            }
//        )
//    ),
//
//    PLAYER_TIME(
//        PlayerOperator(
//            {
//                it.playerTime
//            },
//            { p, a, v ->
//                p.setPlayerTime(io.izzel.taboolib.util.Coerce.toLong(v), a == Symbol.ADD)
//            }
//        )
//    ),
//
//    FIRST_PLAYED(
//        PlayerOperator(
//            {
//                it.firstPlayed
//            }
//        )
//    ),
//
//    LAST_PLAYED(
//        PlayerOperator(
//            {
//                it.lastPlayed
//            }
//        )
//    ),
//
//    LAST_LOGIN(
//        PlayerOperator(
//            {
//                it.lastLogin
//            }
//        )
//    ),
//
//    LAST_SEEN(
//        PlayerOperator(
//            {
//                it.lastSeen
//            }
//        )
//    ),
//
//    ABSORPTION_AMOUNT(
//        PlayerOperator(
//            {
//                it.absorptionAmount
//            },
//            { p, a, v ->
//                if (a == Symbol.ADD) {
//                    p.absorptionAmount += io.izzel.taboolib.util.Coerce.toDouble(v)
//                } else if (a == Symbol.SET) {
//                    p.absorptionAmount = io.izzel.taboolib.util.Coerce.toDouble(v)
//                }
//            }
//        )
//    ),
//
//    NO_DAMAGE_TICKS(
//        PlayerOperator(
//            {
//                it.noDamageTicks
//            },
//            { p, a, v ->
//                if (a == Symbol.ADD) {
//                    p.noDamageTicks += io.izzel.taboolib.util.Coerce.toInteger(v)
//                } else if (a == Symbol.SET) {
//                    p.noDamageTicks = io.izzel.taboolib.util.Coerce.toInteger(v)
//                }
//            }
//        )
//    ),
//
//    REMAINING_AIR(
//        PlayerOperator(
//            {
//                it.remainingAir
//            },
//            { p, a, v ->
//                val d = io.izzel.taboolib.util.Coerce.toInteger(v)
//                if (a == Symbol.ADD) {
//                    p.remainingAir = (p.remainingAir + d).coerceAtMost(20).coerceAtLeast(0)
//                } else if (a == Symbol.SET) {
//                    p.remainingAir = d.coerceAtMost(20).coerceAtLeast(0)
//                }
//            }
//        )
//    ),
//
//    MAXIMUM_AIR(
//        PlayerOperator(
//            {
//                it.maximumAir
//            },
//            { p, a, v ->
//                val d = io.izzel.taboolib.util.Coerce.toInteger(v)
//                if (a == Symbol.ADD) {
//                    p.maximumAir = (p.maximumAir + d).coerceAtMost(20).coerceAtLeast(0)
//                } else if (a == Symbol.SET) {
//                    p.maximumAir = d.coerceAtMost(20).coerceAtLeast(0)
//                }
//            }
//        )
//    ),
//
//    EXP_UNTIL_NEXT_LEVEL(
//        PlayerOperator(
//            {
//                io.izzel.taboolib.cronus.CronusUtils.getExpUntilNextLevel(it)
//            }
//        )
//    ),
//
//    EXP_AT_LEVEL(
//        PlayerOperator(
//            {
//                io.izzel.taboolib.cronus.CronusUtils.getExpAtLevel(it.level)
//            }
//        )
//    ),
//
//    EXP_TO_LEVEL(
//        PlayerOperator(
//            {
//                io.izzel.taboolib.cronus.CronusUtils.getExpToLevel(it.level)
//            }
//        )
//    ),
//
//    EXP(
//        PlayerOperator(
//            {
//                io.izzel.taboolib.cronus.CronusUtils.getTotalExperience(it)
//            },
//            { p, a, v ->
//                if (a == Symbol.ADD) {
//                    p.giveExp(io.izzel.taboolib.util.Coerce.toInteger(v))
//                } else if (a == Symbol.SET) {
//                    io.izzel.taboolib.cronus.CronusUtils.setTotalExperience(p, io.izzel.taboolib.util.Coerce.toInteger(v))
//                }
//            }
//        )
//    ),
//
//    LEVEL(
//        PlayerOperator(
//            {
//                it.level
//            },
//            { p, a, v ->
//                if (a == Symbol.ADD) {
//                    p.level += io.izzel.taboolib.util.Coerce.toInteger(v)
//                } else if (a == Symbol.SET) {
//                    p.level = io.izzel.taboolib.util.Coerce.toInteger(v)
//                }
//            }
//        )
//    ),
//
//    EXHAUSTION(
//        PlayerOperator(
//            {
//                it.exhaustion
//            },
//            { p, a, v ->
//                val f = io.izzel.taboolib.util.Coerce.toFloat(v)
//                if (a == Symbol.ADD) {
//                    p.exhaustion = (p.exhaustion + f).coerceAtMost(20f).coerceAtLeast(0f)
//                } else if (a == Symbol.SET) {
//                    p.exhaustion = f.coerceAtMost(20f).coerceAtLeast(0f)
//                }
//            }
//        )
//    ),
//
//    SATURATION(
//        PlayerOperator(
//            {
//                it.saturation
//            },
//            { p, a, v ->
//                val f = io.izzel.taboolib.util.Coerce.toFloat(v)
//                if (a == Symbol.ADD) {
//                    p.saturation = (p.saturation + f).coerceAtMost(20f).coerceAtLeast(0f)
//                } else if (a == Symbol.SET) {
//                    p.saturation = f.coerceAtMost(20f).coerceAtLeast(0f)
//                }
//            }
//        )
//    ),
//
//    FOOD_LEVEL(
//        PlayerOperator(
//            {
//                it.foodLevel
//            },
//            { p, a, v ->
//                val d = io.izzel.taboolib.util.Coerce.toInteger(v)
//                if (a == Symbol.ADD) {
//                    p.foodLevel = (p.foodLevel + d).coerceAtMost(20).coerceAtLeast(0)
//                } else if (a == Symbol.SET) {
//                    p.foodLevel = d.coerceAtMost(20).coerceAtLeast(0)
//                }
//            }
//        )
//    ),
//
//    HEALTH(
//        PlayerOperator(
//            {
//                it.health
//            },
//            { p, a, v ->
//                val d = io.izzel.taboolib.util.Coerce.toDouble(v)
//                if (a == Symbol.ADD) {
//                    p.health = (p.health + d).coerceAtMost(p.maxHealth).coerceAtLeast(0.0)
//                } else if (a == Symbol.SET) {
//                    p.health = d.coerceAtMost(p.maxHealth).coerceAtLeast(0.0)
//                }
//            }
//        )
//    ),
//
//    MAX_HEALTH(
//        PlayerOperator(
//            {
//                it.maxHealth
//            },
//            { p, a, v ->
//                if (a == Symbol.ADD) {
//                    p.maxHealth += io.izzel.taboolib.util.Coerce.toDouble(v)
//                } else if (a == Symbol.SET) {
//                    p.maxHealth = io.izzel.taboolib.util.Coerce.toDouble(v)
//                }
//            }
//        )
//    ),
//
//    ALLOW_FLIGHT(
//        PlayerOperator(
//            {
//                it.allowFlight
//            },
//            { p, _, v ->
//                p.allowFlight = io.izzel.taboolib.util.Coerce.toBoolean(v)
//            }
//        )
//    ),
//
//    FLYING(
//        PlayerOperator(
//            {
//                it.isFlying
//            },
//            { p, _, v ->
//                p.isFlying = io.izzel.taboolib.util.Coerce.toBoolean(v)
//            }
//        )
//    ),
//
//    FLY_SPEED(
//        PlayerOperator(
//            {
//                it.flySpeed
//            },
//            { p, a, v ->
//                val f = io.izzel.taboolib.util.Coerce.toFloat(v)
//                if (a == Symbol.ADD) {
//                    p.flySpeed = (p.flySpeed + f).coerceAtMost(0.99f).coerceAtLeast(0f)
//                } else if (a == Symbol.SET) {
//                    p.flySpeed = f.coerceAtMost(0.99f).coerceAtLeast(0f)
//                }
//            }
//        )
//    ),
//
//    WALK_SPEED(
//        PlayerOperator(
//            {
//                it.walkSpeed
//            },
//            { p, a, v ->
//                val f = io.izzel.taboolib.util.Coerce.toFloat(v)
//                if (a == Symbol.ADD) {
//                    p.walkSpeed = (p.walkSpeed + f).coerceAtMost(0.99f).coerceAtLeast(0f)
//                } else if (a == Symbol.SET) {
//                    p.walkSpeed = f.coerceAtMost(0.99f).coerceAtLeast(0f)
//                }
//            }
//        )
//    ),
//
//    BALANCE(
//        PlayerOperator(
//            {
//                TabooLibAPI.getPluginBridge().economyLook(it)
//            },
//            { p, a, v ->
//                if (a == Symbol.ADD) {
//                    io.izzel.taboolib.module.compat.EconomyHook.add(p, io.izzel.taboolib.util.Coerce.toDouble(v))
//                } else if (a == Symbol.SET) {
//                    io.izzel.taboolib.module.compat.EconomyHook.set(p, io.izzel.taboolib.util.Coerce.toDouble(v))
//                }
//            }
//        )
//    ),
//
//    REGION(
//        PlayerOperator(
//            {
//                val region = TabooLibAPI.getPluginBridge().worldguardGetRegion(it.world, it.location)
//                if (region!!.isEmpty()) "__global__" else region
//            }
//        )
//    ),
//
//    PROTOCOL_VERSION(
//        PlayerOperator(
//            {
//                it.protocolVersion
//            }
//        )
//    ),
//
//    VERSION(
//        PlayerOperator(
//            {
//                TabooLibAPI.getPluginBridge().viaVersionPlayerVersion(it)
//            }
//        )
//    ),
//
//    PING(
//        PlayerOperator(
//            {
//                it.spigot().ping
//            }
//        )
//    ),
//
//    POSE(
//        PlayerOperator(
//            {
//                it.pose.name
//            }
//        )
//    ),
//
//    FACING(
//        PlayerOperator(
//            {
//                it.facing.name
//            }
//        )
//    ),
//
//    IN_LAVA(
//        PlayerOperator(
//            {
//                it.isInLava
//            }
//        )
//    ),
//
//    IN_RAIN(
//        PlayerOperator(
//            {
//                it.isInRain
//            }
//        )
//    ),
//
//    IN_WATER(
//        PlayerOperator(
//            {
//                it.isInWater
//            }
//        )
//    ),
//
//    IN_BUBBLE_COLUMN(
//        PlayerOperator(
//            {
//                it.isInBubbleColumn
//            }
//        )
//    ),
//
//    IN_WATER_OR_RAIN(
//        PlayerOperator(
//            {
//                it.isInWaterOrRain
//            }
//        )
//    ),
//
//    IN_WATER_OR_BUBBLE_COLUMN(
//        PlayerOperator(
//            {
//                it.isInWaterOrBubbleColumn
//            }
//        )
//    ),
//
//    IN_WATER_OR_RAIN_OR_BUBBLE_COLUMN(
//        PlayerOperator(
//            {
//                it.isInWaterOrRainOrBubbleColumn
//            }
//        )
//    ),
}
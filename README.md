![](https://wiki.ptms.ink/images/6/69/Taboolib-png-blue-v2.png)

## TabooLib Framework 

[**[中文版本/Chinese Ver.]**](https://github.com/TabooLib/TabooLib/blob/master/README-CN.md)

[![](https://app.codacy.com/project/badge/Grade/3e9c747cd4aa484ab7cd74b7666c4c43)](https://www.codacy.com/gh/TabooLib/TabooLib/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=TabooLib/TabooLib&amp;utm_campaign=Badge_Grade)
[![](https://www.codefactor.io/repository/github/taboolib/taboolib/badge)](https://www.codefactor.io/repository/github/taboolib/taboolib)
![](https://img.shields.io/github/contributors/taboolib/taboolib)
![](https://img.shields.io/github/languages/code-size/taboolib/taboolib)

TabooLib is a multi-platform plugin development framework for Minecraft Java Version. However, TabooLib itself is neither a platform nor a runtime environment for plugins, but a tool designed to help developers speed up development on various platforms, replacing some frequently used or relatively complex operations, as well as solving some painful problems.

+ TabooLib started out for Bukkit, but now is developing horizontally.
+ TabooLib is offered under the MIT License, which is a loose open source license.
+ Development speed comes first

Along with the 6.0 update, we focused more on security and stability. The hot-loading system which was problematic in the previous version has been abandoned. While it significantly reduced the size of plugins and introduced a centralized plugin manager, with the advent of updates to Minecraft and a multitude of derivatives of Spigot, this has become troublesome. So it was a foregone conclusion that a huge update to v6.0 was in order, in which we carefully redesigned every single component of TabooLib.

Most TabooLib-based plugins are supposed to work across multiple Minecraft versions without special updates. i.e. in most cases, server owners would not need to be concerned about incompatibility of plugins. Even with extensive use of nms code, TabooLib provides several magical tools.

**Simpler, for example you can quickly register commands using the method provided in TabooLib.**

```kotlin
command("tpuuid") {
    literal("random") {
        execute<ProxyPlayer> { player, _, _ ->
            player.teleport(player.entities().randomOrNull() ?: return@execute)
        }
    }
    dynamic(optional = true) {
        suggestion<ProxyPlayer> { player, _ ->
            player.entities().map { it.toString() }
        }
        execute<ProxyPlayer> { player, _, argument ->
            player.teleport(UUID.fromString(argument))
        }
    }
    execute<ProxyPlayer> { player, _, _ ->
        player.teleport(player.entityNearly() ?: return@execute)
    }
}
```

For more complex ones, you are able to create your own multi-platform implementation like the following, where TabooLib will select the appropriate implementation class based on the platform currently running.

```kotlin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.*
import taboolib.platform.util.toBukkitLocation
import java.util.*

interface PlatformEntityHandler {

    fun entities(player: ProxyPlayer): List<UUID>

    fun entityNearly(player: ProxyPlayer): UUID?

    fun teleport(player: ProxyPlayer, uuid: UUID)

    @PlatformImplementation(Platform.BUKKIT)
    class BukkitSide : PlatformEntityHandler {

        override fun entities(player: ProxyPlayer): List<UUID> {
            return player.cast<Player>().world.entities.map { it.uniqueId }
        }

        override fun entityNearly(player: ProxyPlayer): UUID? {
            return player.cast<Player>().world.entities
                .filter { it != player.origin }
                .minByOrNull { it.location.distance(player.location.toBukkitLocation()) }?.uniqueId
        }

        override fun teleport(player: ProxyPlayer, uuid: UUID) {
            player.cast<Player>().teleport(Bukkit.getEntity(uuid) ?: return)
        }
    }
}

fun ProxyPlayer.entities(): List<UUID> {
    return implementations<PlatformEntityHandler>().entities(this)
}

fun ProxyPlayer.entityNearly(): UUID? {
    return implementations<PlatformEntityHandler>().entityNearly(this)
}

fun ProxyPlayer.teleport(uuid: UUID) {
    implementations<PlatformEntityHandler>().teleport(this, uuid)
}
```

There is no need to do so if your plugin is designed to work only on the Bukkit platform. Since the duty of TabooLib is to help developers get their development done as fast as possible, instead of creating pointless methods to increase the size of the repository.

## Versions

| Build Version | Distribution Date | Distributor | Plugin Version |
| --- | --- | --- | --- |
| ![](https://img.shields.io/badge/dynamic/json?label=Version&query=%24.tag_name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FTabooLib%2FTabooLib%2Freleases%2Flatest) | ![](https://img.shields.io/badge/dynamic/json?label=Date&query=%24.created_at&url=https%3A%2F%2Fapi.github.com%2Frepos%2FTabooLib%2FTabooLib%2Freleases%2Flatest) | ![](https://img.shields.io/badge/dynamic/json?label=Author&query=%24.author.login&url=https%3A%2F%2Fapi.github.com%2Frepos%2FTabooLib%2FTabooLib%2Freleases%2Flatest) | ![](https://img.shields.io/badge/dynamic/json?label=Plugin&query=%24.tag_name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FTabooLib%2Ftaboolib-gradle-plugin%2Freleases%2Flatest) |

## Modules

+ __common__: Core parts of TabooLib, the environment deployment and cross-platform interface
+ __common-5__: Some tools retained from TabooLib v5.0
+ __module-ai__: Manage and register custom entity AI (Pathfinder)
+ __module-chat__: Building Tools for Component (Json) Information & 1.16 RGB Color Transformations
+ __module-configuration__: Solutions for Configuration（Yaml & Toml & Hocon & Json)
+ __module-configuration-legacy__: YAML Interface Wrappers & Configuration Management Tools ( previous version, before v6.0.3)
+ __module-database__: Database Management Tools
+ __module-database-mongodb__: Database Management Tools（MongoDB）
+ __module-effect__: Particles Generation Utilities
+ __module-kether__: Build-in scripts (action statements) solutions
+ __module-lang__: Language File Utilities
+ __module-metrics__: Integration of bStats
+ __module-navigation__: Entity-less Pathfinding Utilities
+ __module-nms__: Multi-version NMS Solutions & Packet Management Tools
+ __module-nms-util__: Collection of Common NMS Tools
+ __module-porticus__: BungeeCord Communication Tools
+ __module-ui__: Chest Menu Builder
+ __module-ui-receptacle__: Chest Menu Builder Implemented with Packet
+ __platform-bukkit__: Bukkit Implementation
+ __platform-bungee__: BungeeCord Implementation
+ __platform-nukkit__: Nukkit Implementation
+ __platform-sponge-api7__: Sponge (api7) Implementation
+ __platform-sponge-api8__: Sponge (api8) Implementation
+ __platform-sponge-api9__: Sponge (api9) Implementation
+ __platform-velocity__: Velocity Implementation
+ __platform-cloudnet-v3__: CloudNet (v3) Implementation
+ __platform-application__: Standalone Application Implementation

## Links

+ [TabooLib Project Generator](https://get.tabooproject.org)
+ [TabooLib DOCs](https://docs.tabooproject.org)
+ [TabooLib SDK](https://github.com/taboolib/taboolib-sdk)


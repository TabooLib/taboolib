rootProject.name = "TabooLib"

fun importModules() {
    include("module:module-empty")
    include("module:module-ai")
    include("module:module-ui")
    include("module:module-ui-receptacle")
    include("module:module-nms")
    include("module:module-nms-util")
    include("module:module-chat")
    include("module:module-lang")
    include("module:module-effect")
    include("module:module-kether")
    include("module:module-metrics")
    include("module:module-database")
    include("module:module-database-mongodb")
    include("module:module-porticus")
    include("module:module-navigation")
    include("module:module-configuration")
}

fun importPlatforms() {
    include("platform:platform-bukkit", "platform:platform-nukkit", "platform:platform-bungee")
    include("platform:platform-sponge-api7", "platform:platform-sponge-api8")
     include("platform:platform-velocity")
    include("platform:platform-cloudnet-v3")
    include("platform:platform-application")
}

fun importExtensions() {
    // 临时位置，未来会被移出标准模块
    include("expansion:expansion-command-helper")
    include("expansion:expansion-player-database")
}

include("common", "common-5")

importModules()
importPlatforms()
importExtensions()

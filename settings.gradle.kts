rootProject.name = "TabooLib"

fun importModules() {
    include("modules:module-empty")
    include("modules:module-ai")
    include("modules:module-ui")
    include("modules:module-ui-receptacle")
    include("modules:module-nms")
    include("modules:module-nms-util")
    include("modules:module-chat")
    include("modules:module-lang")
    include("modules:module-effect")
    include("modules:module-kether")
    include("modules:module-metrics")
    include("modules:module-database")
    include("modules:module-database-mongodb")
    include("modules:module-porticus")
    include("modules:module-navigation")
    include("modules:module-configuration")
}

fun importPlatforms() {
    include("platform:platform-bukkit", "platform:platform-nukkit", "platform:platform-bungee", "platform:platform-velocity")
    include("platform:platform-sponge-api7", "platform:platform-sponge-api8")
    include("platform:platform-application")
}

fun importExtensions() {
    // 临时位置，未来会被移出标准模块
    include("expansion-command-helper")
}

include("common", "common-5")

importModules()
importPlatforms()
importExtensions()
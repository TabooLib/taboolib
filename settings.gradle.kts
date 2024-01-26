rootProject.name = "TabooLib"

fun importModules() {
    include("module:module-empty")
    include("module:module-ai")
    include("module:module-nms")
    include("module:module-nms-util")
    include("module:module-chat")
    include("module:module-lang")
    include("module:module-effect")
    include("module:module-kether")
    include("module:module-metrics")
    include("module:module-database")
    include("module:module-database-core")
    include("module:module-database-shaded")
    include("module:module-database-mongodb")
    include("module:module-porticus")
    include("module:module-navigation")
    include("module:module-ui", "module:module-ui-legacy")
    include("module:module-ui-receptacle")
    include("module:module-configuration", "module:module-configuration-shaded", "module:module-configuration-legacy")
}

fun importPlatforms() {
    include("platform:platform-bukkit", "platform:platform-bungee")
    include("platform:platform-velocity")
    include("platform:platform-application")
}

fun importExtensions() {
    // 临时位置，未来会被移出标准模块
    include("expansion:expansion-command-helper")
    include("expansion:expansion-player-database")
    include("expansion:expansion-persistent-container")
    include("expansion:expansion-persistent-container-object")
    include("expansion:expansion-alkaid-redis")
    include("expansion:expansion-geek-tool")
    include("expansion:expansion-lang-tools")
    include("expansion:expansion-ioc")
    include("expansion:expansion-application-console")
    include("expansion:expansion-player-fake-op")
    include("expansion:expansion-submit-chain")
    // 从 common-legacy-api 中移除
    include("expansion:expansion-javascript")
}

//importModules()
//importPlatforms()
//importExtensions()

include("common", "common-env", "common-util", "common-legacy-api", "common-reflex", "common-platform-api")
include(
    "module:module-ai",
    "module:module-chat",
    "module:module-configuration",
    "module:module-lang",
    "module:module-metrics",
    "module:module-navigation",
    "module:module-bukkit-hook",
    "module:module-bukkit-util",
    "module:module-bukkit-xseries",
    "module:module-database",
    "module:module-effect",
    "module:module-kether",
    "module:module-nms",
    "module:module-nms-util",
    "module:module-porticus",
    "module:module-ui",
    "module:module-ui-legacy"
)
include(
    "expansion:expansion-javascript"
)
include(
    "platform:platform-application",
    "platform:platform-bukkit",
    "platform:platform-bukkit-impl",
    "platform:platform-bungee",
    "platform:platform-bungee-impl",
    "platform:platform-velocity",
    "platform:platform-velocity-impl"
)
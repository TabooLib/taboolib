rootProject.name = "TabooLib"
include("common", "common-env", "common-util", "common-legacy-api", "common-reflex", "common-platform-api")
include(
    "module:basic-configuration",
    "module:basic-submit-chain",
    "module:bukkit-hook",
//    "module:bukkit-navigation",
//    "module:bukkit-ui",
//    "module:bukkit-ui-legacy",
//    "module:bukkit-ui-modern",
//    "module:bukkit-util",
//    "module:bukkit-xseries",
//    "module:bukkit-xseries-item",
//    "module:bukkit-xseries-skull",
    "module:database-sql",
    "module:minecraft-chat",
    "module:minecraft-command-helper",
    "module:minecraft-effect",
    "module:minecraft-i18n",
//    "module:minecraft-kether",
    "module:minecraft-metrics",
    "module:minecraft-porticus",
//    "module:nms",
//    "module:nms-util",
//    "module:nms-util-ai",
//    "module:nms-util-legacy",
//    "module:nms-util-tag",
//    "module:nms-util-tag-12005",
//    "module:nms-util-tag-legacy",
//    "module:nms-util-unstable",
//    "module:test",
)
include(
)
include(
    "platform:platform-application",
    "platform:platform-bukkit",
    "platform:platform-bukkit-impl",
    "platform:platform-bungee",
    "platform:platform-bungee-impl",
    "platform:platform-velocity",
    "platform:platform-velocity-impl",
    "platform:platform-afybroker"
)

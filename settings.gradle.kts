rootProject.name = "TabooLib"

//common
include("common:common-adapter")
include("common:common-command")
include("common:common-command-annotation")
include("common:common-core")
include("common:common-core-impl")
include("common:common-environment")
include("common:common-event")
include("common:common-listener")
include("common:common-openapi")
include("common:common-plugin")
include("common:common-scheduler")
include("common:common-util")

//module
include("module:module-chat")
include("module:module-configuration:module-configuration-core")
include("module:module-configuration:module-configuration-core-shaded")

//platform-bukkit
include("platform:platform-bukkit:platform-bukkit-adapter")
include("platform:platform-bukkit:platform-bukkit-command")
include("platform:platform-bukkit:platform-bukkit-core")
include("platform:platform-bukkit:platform-bukkit-event")
include("platform:platform-bukkit:platform-bukkit-listener")
include("platform:platform-bukkit:platform-bukkit-openapi")
include("platform:platform-bukkit:platform-bukkit-plugin")
include("platform:platform-bukkit:platform-bukkit-scheduler")

//platform-bungee
include("platform:platform-bungee:platform-bungee-adapter")
include("platform:platform-bungee:platform-bungee-command")
include("platform:platform-bungee:platform-bungee-core")
include("platform:platform-bungee:platform-bungee-event")
include("platform:platform-bungee:platform-bungee-listener")
include("platform:platform-bungee:platform-bungee-openapi")
include("platform:platform-bungee:platform-bungee-plugin")
include("platform:platform-bungee:platform-bungee-scheduler")

//platform-cloudnet
include("platform:platform-cloudnet-v3:platform-cloudnet-v3-adapter")
include("platform:platform-cloudnet-v3:platform-cloudnet-v3-command")
include("platform:platform-cloudnet-v3:platform-cloudnet-v3-core")
include("platform:platform-cloudnet-v3:platform-cloudnet-v3-event")
include("platform:platform-cloudnet-v3:platform-cloudnet-v3-listener")
include("platform:platform-cloudnet-v3:platform-cloudnet-v3-openapi")
include("platform:platform-cloudnet-v3:platform-cloudnet-v3-plugin")
include("platform:platform-cloudnet-v3:platform-cloudnet-v3-scheduler")

//platform-nukkit
include("platform:platform-nukkit:platform-nukkit-adapter")
include("platform:platform-nukkit:platform-nukkit-command")
include("platform:platform-nukkit:platform-nukkit-core")
include("platform:platform-nukkit:platform-nukkit-event")
include("platform:platform-nukkit:platform-nukkit-listener")
include("platform:platform-nukkit:platform-nukkit-openapi")
include("platform:platform-nukkit:platform-nukkit-plugin")
include("platform:platform-nukkit:platform-nukkit-scheduler")

//platform-velocity
include("platform:platform-velocity:platform-velocity-adapter")
include("platform:platform-velocity:platform-velocity-command")
include("platform:platform-velocity:platform-velocity-core")
include("platform:platform-velocity:platform-velocity-event")
include("platform:platform-velocity:platform-velocity-listener")
include("platform:platform-velocity:platform-velocity-openapi")
include("platform:platform-velocity:platform-velocity-plugin")
include("platform:platform-velocity:platform-velocity-scheduler")
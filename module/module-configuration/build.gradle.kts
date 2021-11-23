dependencies {
    compileOnly("com.typesafe:config:1.4.1")
    compileOnly("com.amihaiemil.web:eo-yaml:6.0.0")
    compileOnly("com.electronwill.night-config:core:3.6.5")
    compileOnly("com.electronwill.night-config:toml:3.6.5")
    compileOnly("com.electronwill.night-config:json:3.6.5")
    compileOnly("com.electronwill.night-config:hocon:3.6.5")
    compileOnly(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module:module-chat"))
}
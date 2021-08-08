import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("io.izzel.kether:common:1.0.16")
    compileOnly("public:PlaceholderAPI:2.10.9")
    compileOnly("ink.ptms.core:v11200:11200:all")
    compileOnly("com.google.guava:guava:17.0")
    compileOnly("org.apache.commons:commons-lang3:3.5")
    compileOnly(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module-chat"))
    compileOnly(project(":module-lang"))
    compileOnly(project(":module-nms-util"))
    compileOnly(project(":module-configuration"))
    compileOnly(kotlin("stdlib"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        dependencies {
            include(dependency("io.izzel.kether:common:1.0.16"))
        }
        exclude("io/izzel/kether/common/util/Coerce.class")
        exclude("LICENSE")
        exclude("LICENSE-Coerce")
        relocate("LICENSE", "LICENSE-Kether")
        // openapi
        relocate("taboolib.module.kether.ScriptProperty", "openapi.kether.ScriptProperty")
        relocate("taboolib.module.kether.ScriptActionParser", "openapi.kether.ScriptActionParser")
        relocate("io.izzel.kether.common.loader.QuestReader", "openapi.kether.QuestReader")
        relocate("io.izzel.kether.common.loader.LoadError", "openapi.kether.LoadError")
        relocate("io.izzel.kether.common.loader.ArgType", "openapi.kether.ArgType")
        relocate("io.izzel.kether.common.loader.types", "openapi.kether")
        relocate("io.izzel.kether.common.util.LocalizedException", "openapi.kether.LocalizedException")
        relocate("io.izzel.kether.common.api.ServiceHolder", "openapi.kether.ServiceHolder")
        relocate("io.izzel.kether.common.api.ParsedAction", "openapi.kether.ParsedAction")
        relocate("io.izzel.kether.common.api.Quest", "openapi.kether.Quest")
        relocate("io.izzel.kether.common.api.data.ExitStatus", "openapi.kether.ExitStatus")
        relocate("io.izzel.kether.common.api.data.QuestFuture", "openapi.kether.QuestFuture")
        relocate("io.izzel.kether.common.api.data.VarString", "openapi.kether.VarString")
        // relocate
        relocate("io.izzel.kether.common.util.Coerce", "taboolib.common5.Coerce")
        relocate("io.izzel.kether.common.api.data", "taboolib.library.kether")
        relocate("io.izzel.kether.common.api", "taboolib.library.kether")
        relocate("io.izzel.kether.common.util", "taboolib.library.kether")
        relocate("io.izzel.kether.common.loader.types", "taboolib.library.kether")
        relocate("io.izzel.kether.common.loader", "taboolib.library.kether")
        relocate("io.izzel.kether.common.actions", "taboolib.library.kether.actions")
    }
    build {
        dependsOn(shadowJar)
    }
}
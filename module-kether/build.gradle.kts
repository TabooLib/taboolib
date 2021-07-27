import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("io.izzel.kether:common:1.0.15")
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
            include(dependency("io.izzel.kether:common:1.0.15"))
        }
        exclude("io/izzel/kether/common/util/Coerce.class")
        exclude("LICENSE")
        exclude("LICENSE-Coerce")
        relocate("LICENSE", "LICENSE-Kether")
        // openapi
        relocate("taboolib.module.kether.ScriptProperty", "openapi.kether.ScriptProperty")
        relocate("taboolib.module.kether.ScriptActionParser", "openapi.kether.ScriptActionParser")
        relocate("io.izzel.kether.common.api.QuestActionParser", "openapi.kether.QuestActionParser")
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
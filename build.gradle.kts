import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.tabooproject.shrinkingkt.ShrinkingExt

plugins {
    `maven-publish`
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    id("org.jetbrains.kotlin.jvm") version "1.6.10" apply false
    id("org.tabooproject.shrinkingkt") version "1.0.6" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

detekt {
    parallel = true
    config = files("detekt.yml")
    buildUponDefaultConfig = true
    source = files(subprojects.map(Project::getName).map { "$it/src/main/kotlin" })
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.tabooproject.shrinkingkt")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        maven("https://repo.tabooproject.org/repository/releases/")
        mavenCentral()
    }

    dependencies {
        "compileOnly"(kotlin("stdlib"))
        "compileOnly"("org.tabooproject.reflex:analyser:1.0.9")
        "compileOnly"("org.tabooproject.reflex:reflex:1.0.9")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.8.1")
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.8.1")
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    configure<ShrinkingExt> {
        annotation = "taboolib.internal.Internal"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("org.tabooproject.reflex", "taboolib.common.reflect")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-XDenableSunApiLintControl"))
    }

    tasks.withType<KotlinCompile> {
        dependsOn("detekt")
        kotlinOptions.freeCompilerArgs += listOf("-module-name", "${project.group}.${project.name}")
    }

    tasks.withType<Detekt> {
        jvmTarget = "1.8"
    }

    publishing {
        repositories {
            maven("http://ptms.ink:8081/repository/releases") {
                isAllowInsecureProtocol = true
                credentials {
                    username = project.findProperty("taboolibUsername").toString()
                    password = project.findProperty("taboolibPassword").toString()
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}

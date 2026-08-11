import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import ru.vyarus.gradle.plugin.animalsniffer.AnimalSnifferExtension
import java.io.DataInputStream
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    `maven-publish`
    java
    id("org.jetbrains.kotlin.jvm") version "1.8.22" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("ru.vyarus.animalsniffer") version "2.0.1" apply false
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "maven-publish")
    apply(plugin = "ru.vyarus.animalsniffer")

    repositories {
        maven("https://jitpack.io")
        maven("https://libraries.minecraft.net")
        maven("https://repo1.maven.org/maven2")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://repo.tabooproject.org/repository/releases")
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compileOnly(kotlin("stdlib"))
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        compileOnly("com.google.guava:guava:21.0")
        compileOnly("com.google.code.gson:gson:2.8.7")
        compileOnly("org.apache.commons:commons-lang3:3.5")
        compileOnly("org.tabooproject.reflex:reflex:1.2.4")
        compileOnly("org.tabooproject.reflex:analyser:1.2.4")
        add("signature", "org.codehaus.mojo.signature:java18:1.0@signature")
        // 测试依赖
        testImplementation(kotlin("stdlib"))
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        testImplementation("com.google.guava:guava:21.0")
        testImplementation("com.google.code.gson:gson:2.8.7")
        testImplementation("org.apache.commons:commons-lang3:3.5")
        testImplementation("org.tabooproject.reflex:reflex:1.2.4")
        testImplementation("org.tabooproject.reflex:analyser:1.2.4")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    }

    java {
        withSourcesJar()
    }

    configure<AnimalSnifferExtension> {
        ignore = listOf(
            "java.lang.invoke.MethodHandle",
            "co.*",
            "com.*",
            "dev.*",
            "ink.*",
            "io.*",
            "it.*",
            "kotlin.*",
            "kotlinx.*",
            "me.*",
            "net.*",
            "org.*",
            "reactor.*",
            "redis.*",
            "taboolib.*",
        )
        excludeJars = listOf("v260100-260100-minimize")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // Gradle 8.9：test 类路径消费依赖工程 shadowJar（classifier 为空）时须显式 dependsOn
    afterEvaluate {
        tasks.withType<Test>().configureEach {
            val shadowTasks = configurations.findByName("testImplementation")?.dependencies
                ?.filterIsInstance<org.gradle.api.artifacts.ProjectDependency>()
                ?.mapNotNull { dep ->
                    val depProject = dep.dependencyProject
                    depProject.tasks.findByName("shadowJar")?.let { depProject.tasks.named("shadowJar") }
                }
                ?: emptyList()
            if (shadowTasks.isNotEmpty()) {
                dependsOn(shadowTasks)
            }
        }
    }

    tasks.withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("org.tabooproject", "taboolib.library")
    }

    tasks.named<Jar>("jar") {
        archiveClassifier.set("plain")
    }

    tasks.build {
        dependsOn("shadowJar")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(8)
        options.compilerArgs.addAll(listOf("-XDenableSunApiLintControl"))
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}

data class MavenCoordinate(val groupId: String, val artifactId: String, val version: String)

fun Project.publishedArtifactId(): String {
    val extra = extensions.extraProperties
    return if (extra.has("publishId")) extra.get("publishId").toString() else name
}

fun Project.publishedVersion(): String {
    return when {
        rootProject.hasProperty("devLocal") -> "${version}-local-dev"
        rootProject.hasProperty("dev") -> "${version}-dev"
        else -> version.toString()
    }
}

fun Project.isPublishableModule(): Boolean {
    if (name == "module" || name == "platform" || name == "expansion" || name.startsWith("impl")) {
        return false
    }
    val mainSourceSet = extensions.getByType<SourceSetContainer>().getByName("main")
    val hasMainContent = mainSourceSet.allSource.srcDirs.any { sourceDirectory ->
        sourceDirectory.isDirectory && sourceDirectory.walkTopDown().any(File::isFile)
    }
    return hasMainContent || path == ":common-reflex"
}

fun Project.apiPomCoordinates(): List<MavenCoordinate> {
    val publicDependencies = configurations.getByName("api").dependencies +
        configurations.getByName("compileOnlyApi").dependencies
    return publicDependencies.mapNotNull { dependency ->
        when (dependency) {
            is ProjectDependency -> {
                val dependencyProject = dependency.dependencyProject
                MavenCoordinate("io.izzel.taboolib", dependencyProject.publishedArtifactId(), dependencyProject.publishedVersion())
            }
            is ExternalModuleDependency -> {
                val groupId = dependency.group ?: return@mapNotNull null
                val version = dependency.version ?: return@mapNotNull null
                MavenCoordinate(groupId, dependency.name, version)
            }
            else -> null
        }
    }.distinct().sortedWith(compareBy(MavenCoordinate::groupId, MavenCoordinate::artifactId, MavenCoordinate::version))
}

fun readPomCoordinates(pomFile: File): Set<MavenCoordinate> {
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pomFile)
    val dependencies = document.getElementsByTagName("dependency")
    return buildSet {
        for (index in 0 until dependencies.length) {
            val dependency = dependencies.item(index)
            val children = dependency.childNodes
            var groupId: String? = null
            var artifactId: String? = null
            var version: String? = null
            for (childIndex in 0 until children.length) {
                val child = children.item(childIndex)
                when (child.nodeName) {
                    "groupId" -> groupId = child.textContent.trim()
                    "artifactId" -> artifactId = child.textContent.trim()
                    "version" -> version = child.textContent.trim()
                }
            }
            if (groupId != null && artifactId != null && version != null) {
                add(MavenCoordinate(groupId, artifactId, version))
            }
        }
    }
}

fun classFileMajorVersion(classFile: File): Int {
    return DataInputStream(classFile.inputStream().buffered()).use { input ->
        check(input.readInt() == 0xCAFEBABE.toInt()) { "Invalid class file: $classFile" }
        input.readUnsignedShort()
        input.readUnsignedShort()
    }
}

val verifyPublishingModel = tasks.register("verifyPublishingModel") {
    group = "verification"
    description = "Verifies published artifacts, excluded projects, and generated Maven dependencies."
}

val verifyJava8Compatibility = tasks.register("verifyJava8Compatibility") {
    group = "verification"
    description = "Verifies Java 8 API gates and generated JVM bytecode versions."
}

tasks.named("check") {
    dependsOn(verifyPublishingModel, verifyJava8Compatibility)
}

subprojects {
    val subProject = this
    afterEvaluate {
        val publishable = subProject.isPublishableModule()
        subProject.extensions.extraProperties.set("taboolibPublishable", publishable)
        if (publishable) {
            subProject.configure<PublishingExtension> { applyToSub(subProject) }
        }
    }
}

gradle.projectsEvaluated {
    val publishableProjects = subprojects.filter {
        it.extensions.extraProperties.get("taboolibPublishable") == true
    }
    val excludedProjects = subprojects - publishableProjects.toSet()

    verifyPublishingModel.configure {
        dependsOn(publishableProjects.map { project ->
            project.tasks.named<GenerateMavenPom>("generatePomFileForMavenPublication")
        })
        doLast {
            publishableProjects.forEach { project ->
                val publication = project.extensions.getByType<PublishingExtension>()
                    .publications.getByName("maven") as MavenPublication
                val classifiers = publication.artifacts.map { artifact ->
                    artifact.classifier?.takeIf(String::isNotBlank) ?: "main"
                }.sorted()
                check(classifiers == listOf("main", "sources")) {
                    "${project.path} must publish one main shadow artifact and one sources artifact, got $classifiers"
                }
                val pomTask = project.tasks.named<GenerateMavenPom>("generatePomFileForMavenPublication").get()
                val expectedDependencies = project.apiPomCoordinates().toSet()
                val actualDependencies = readPomCoordinates(pomTask.destination)
                check(actualDependencies == expectedDependencies) {
                    "${project.path} POM dependencies differ: expected=$expectedDependencies, actual=$actualDependencies"
                }
            }
            excludedProjects.forEach { project ->
                val publications = project.extensions.getByType<PublishingExtension>().publications
                check(publications.isEmpty()) { "${project.path} must not create Maven publications" }
            }
        }
    }

    val compileTasks = subprojects.flatMap { project ->
        project.tasks.withType<JavaCompile>().toList() + project.tasks.withType<KotlinCompile>().toList()
    }
    val animalSnifferTasks = subprojects.mapNotNull { project ->
        project.tasks.findByName("animalsnifferMain")
    }
    verifyJava8Compatibility.configure {
        dependsOn(compileTasks, animalSnifferTasks)
        doLast {
            subprojects.forEach { project ->
                project.tasks.withType<JavaCompile>().forEach { compileTask ->
                    check(compileTask.options.release.orNull == 8) {
                        "${compileTask.path} must compile with --release 8"
                    }
                }
                project.tasks.withType<KotlinCompile>().forEach { compileTask ->
                    check(compileTask.kotlinOptions.jvmTarget == "1.8") {
                        "${compileTask.path} must target JVM 1.8"
                    }
                }
            }
            val classFiles = subprojects.flatMap { project ->
                val classesDirectory = project.layout.buildDirectory.dir("classes").get().asFile
                if (classesDirectory.isDirectory) {
                    classesDirectory.walkTopDown().filter { it.isFile && it.extension == "class" }.toList()
                } else {
                    emptyList()
                }
            }
            check(classFiles.isNotEmpty()) { "No compiled classes found for Java 8 verification" }
            val incompatibleClasses = classFiles.mapNotNull { classFile ->
                val majorVersion = classFileMajorVersion(classFile)
                if (majorVersion == 52) null else "$classFile ($majorVersion)"
            }
            check(incompatibleClasses.isEmpty()) {
                "Non-Java-8 class files found:\n${incompatibleClasses.joinToString("\n")}"
            }
        }
    }
}

fun PublishingExtension.applyToSub(subProject: Project) {
    repositories {
        maven("https://repo.tabooproject.org/repository/releases") {
            credentials {
                username = project.findProperty("taboolibUsername").toString()
                password = project.findProperty("taboolibPassword").toString()
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
//        maven("http://repo.aeoliancloud.com/repository/releases") {
//            isAllowInsecureProtocol = true
//            credentials {
//                username = project.findProperty("aeolianUsername").toString()
//                password = project.findProperty("aeolianPassword").toString()
//            }
//            authentication {
//                create<BasicAuthentication>("basic")
//            }
//        }
        mavenLocal()
    }
    publications {
        create<MavenPublication>("maven") {
            artifactId = subProject.publishedArtifactId()
            groupId = "io.izzel.taboolib"
            version = subProject.publishedVersion()
            artifact(subProject.tasks.named<Jar>("sourcesJar"))
            artifact(subProject.tasks.named<ShadowJar>("shadowJar"))
            val apiDependencies = subProject.apiPomCoordinates()
            if (apiDependencies.isNotEmpty()) {
                pom.withXml {
                    val dependencies = asNode().appendNode("dependencies")
                    apiDependencies.forEach { dependency ->
                        dependencies.appendNode("dependency").apply {
                            appendNode("groupId", dependency.groupId)
                            appendNode("artifactId", dependency.artifactId)
                            appendNode("version", dependency.version)
                            appendNode("scope", "compile")
                        }
                    }
                }
            }
            println("> Apply \"$groupId:$artifactId:$version\"")
        }
    }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}
package taboolib.module.lang

enum class LevelType {

    INFO, WARN, ERROR, DEBUG;

    fun node(): String = "Plugin.${this.name}"
}
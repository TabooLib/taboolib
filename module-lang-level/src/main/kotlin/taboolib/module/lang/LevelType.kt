package taboolib.module.lang

enum class LevelType {

    INFO, WARN, ERROR, DEBUG;

    fun node(): String = "Level-${this.name}"
}
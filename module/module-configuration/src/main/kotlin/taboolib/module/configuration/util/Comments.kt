package taboolib.module.configuration.util

/** 创建包含注释的值 */
infix fun Any.withComment(comment: String): Commented {
    return Commented(this, comment)
}

/** 创建包含注释的值 */
infix fun Any.withComment(comment: List<String>): CommentedList {
    return CommentedList(this, comment)
}

class Commented(val value: Any?, val comment: String)

class CommentedList(val value: Any?, val comment: List<String>)
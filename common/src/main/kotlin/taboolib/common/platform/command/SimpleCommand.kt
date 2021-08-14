package taboolib.common.platform.command

fun mainCommand(func: CommandBuilder.CommandBase.() -> Unit): SimpleCommandMain {
    return SimpleCommandMain(func)
}

fun subCommand(func: CommandBuilder.CommandComponent.() -> Unit): SimpleCommandBody {
    return SimpleCommandBody(func)
}
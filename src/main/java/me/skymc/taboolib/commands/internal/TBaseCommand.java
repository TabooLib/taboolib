package me.skymc.taboolib.commands.internal;

/**
 * @Author sky
 * @Since 2018-05-23 2:43
 */
public class TBaseCommand {

    /**
     * 向服务端注册 BaseMainCommand 类
     *
     * @param command         命令全称（需在 plugin.yml 内注册）
     * @param baseMainCommand 命令对象
     * @return {@link BaseMainCommand}
     */
    public static BaseMainCommand registerCommand(String command, BaseMainCommand baseMainCommand) {
        return BaseMainCommand.createCommandExecutor(command, baseMainCommand);
    }
}

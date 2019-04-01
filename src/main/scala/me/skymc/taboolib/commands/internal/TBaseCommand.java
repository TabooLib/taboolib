package me.skymc.taboolib.commands.internal;

/**
 * 歪日删错了
 *
 * @author sky
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
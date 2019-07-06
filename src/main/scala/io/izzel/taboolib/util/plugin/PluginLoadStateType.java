package io.izzel.taboolib.util.plugin;

/**
 * @Author sky
 * @Since 2018-06-01 21:32
 */
public enum PluginLoadStateType {

    /**
     * 目录不存在
     */
    DIRECTORY_NOT_FOUND,

    /**
     * 插件不存在
     */
    FILE_NOT_FOUND,

    /**
     * 无效的描述
     */
    INVALID_DESCRIPTION,

    /**
     * 无效的插件
     */
    INVALID_PLUGIN,

    /**
     * 载入成功
     */
    LOADED

}

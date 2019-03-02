package com.ilummc.tlib.resources;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @Author sky
 * @Since 2018-05-12 13:58
 */
public interface TLocaleSender {

    /**
     * 发送信息
     *
     * @param sender 发送目标
     * @param args   参数
     */
    void sendTo(CommandSender sender, String... args);

    /**
     * 获取文本
     *
     * @param args 参数
     * @return 文本
     */
    String asString(String... args);

    /**
     * 获取文本集合
     *
     * @param args 参数
     * @return 文本集合
     */
    List<String> asStringList(String... args);

}

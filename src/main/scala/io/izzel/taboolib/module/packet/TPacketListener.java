package io.izzel.taboolib.module.packet;

import org.bukkit.entity.Player;

/**
 * 数据包监听器接口
 *
 * @Author 坏黑
 * @Since 2018-10-28 14:35
 */
public abstract class TPacketListener {

    /**
     * 当数据包发送时
     * 返回 false 则拦截数据包
     *
     * @param player 玩家对象
     * @param packet nms 数据包实例
     * @return 是否发送
     */
    public boolean onSend(Player player, Object packet) {
        return true;
    }

    /**
     * 当数据包发送时
     * 返回 false 则拦截数据包
     *
     * @param player 玩家对象
     * @param packet 数据包实例
     * @return 是否发送
     */
    public boolean onSend(Player player, Packet packet) {
        return true;
    }

    /**
     * 当数据包接收时
     * 返回 false 则拦截数据包
     *
     * @param player 玩家对象
     * @param packet nms 数据包实例
     * @return 是否发送
     */
    public boolean onReceive(Player player, Object packet) {
        return true;
    }

    /**
     * 当数据包接收时
     * 返回 false 则拦截数据包
     *
     * @param player 玩家对象
     * @param packet 数据包实例
     * @return 是否发送
     */
    public boolean onReceive(Player player, Packet packet) {
        return true;
    }
}

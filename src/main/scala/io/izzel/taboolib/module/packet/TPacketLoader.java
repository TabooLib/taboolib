package io.izzel.taboolib.module.packet;

import io.izzel.taboolib.TabooLibLoader;
import io.izzel.taboolib.module.locale.logger.TLogger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * @Author sky
 * @Since 2019-10-25 22:50
 */
public class TPacketLoader implements TabooLibLoader.Loader {

    @Override
    public void activeLoad(Plugin plugin, Class<?> pluginClass) {
        for (Method method : pluginClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(TPacket.class)) {
                Object instance = null;
                // 如果是非静态类型
                if (!Modifier.isStatic(method.getModifiers())) {
                    // 是否为主类
                    if (pluginClass.equals(plugin.getClass())) {
                        instance = plugin;
                    } else {
                        TLogger.getGlobalLogger().error(method.getName() + " is not a static method. (" + pluginClass.getName() + ")");
                        continue;
                    }
                }
                method.setAccessible(true);
                TPacket packet = method.getAnnotation(TPacket.class);
                boolean packetType = method.getReturnType().equals(Boolean.TYPE) || method.getReturnType().equals(Boolean.class);
                // object type
                if (Arrays.equals(method.getParameterTypes(), new Class[] {Player.class, Object.class})) {
                    Object finalInstance = instance;
                    TPacketHandler.addListener(plugin, new TPacketListener() {
                        @Override
                        public boolean onSend(Player player, Object p) {
                            return eval(finalInstance, packet, TPacket.Type.SEND, packetType, method, player, p);
                        }

                        @Override
                        public boolean onReceive(Player player, Object p) {
                            return eval(finalInstance, packet, TPacket.Type.RECEIVE, packetType, method, player, p);
                        }
                    });
                }
                // packet type
                else if (Arrays.equals(method.getParameterTypes(), new Class[] {Player.class, Packet.class})) {
                    Object finalInstance1 = instance;
                    TPacketHandler.addListener(plugin, new TPacketListener() {
                        @Override
                        public boolean onSend(Player player, Packet p) {
                            return eval(finalInstance1, packet, TPacket.Type.SEND, packetType, method, player, p);
                        }

                        @Override
                        public boolean onReceive(Player player, Packet p) {
                            return eval(finalInstance1, packet, TPacket.Type.RECEIVE, packetType, method, player, p);
                        }
                    });
                } else {
                    TLogger.getGlobalLogger().error(method.getName() + " is an invalid packet listener. (" + pluginClass.getName() + ")");
                    TLogger.getGlobalLogger().error("Usage: boolean fun(Player player, Object packet) { ... }");
                }
            }
        }
    }

    private boolean eval(Object instance, TPacket packet, TPacket.Type type, boolean packetType, Method method, Player player, Object obj) {
        if (packet.type() == type) {
            try {
                return !packetType || (boolean) method.invoke(instance, player, obj);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return false;
        }
        return true;
    }
}

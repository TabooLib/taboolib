package io.izzel.taboolib.common.listener;

import io.izzel.taboolib.common.event.PlayerJumpEvent;
import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.util.lite.cooldown.Cooldown;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@TListener
public class ListenerPlayerJump implements Listener {

    @TInject
    private static Cooldown cooldown = new Cooldown("taboolib:jump", 350);

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onJump(PlayerMoveEvent e) {
        // 不是飞行
        if (!e.getPlayer().isFlying()
                // 生存或冒险模式
                && (e.getPlayer().getGameMode() == GameMode.SURVIVAL || e.getPlayer().getGameMode() == GameMode.ADVENTURE)
                // 坐标计算
                && (e.getFrom().getY() + 0.5D != e.getTo().getY())
                && (e.getFrom().getY() + 0.419D < e.getTo().getY())
                // 不在冷却
                && !cooldown.isCooldown(e.getPlayer().getName())) {

            new PlayerJumpEvent(e.getPlayer()).call().ifCancelled(() -> {
                // 返回位置
                e.setTo(e.getFrom());
                // 重置冷却
                cooldown.reset(e.getPlayer().getName());
            });
        }
    }
}

package io.izzel.taboolib.util.lite;

import com.google.common.collect.Lists;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 * @Author 坏黑
 * @Since 2019-07-05 18:53
 */
public class Servers {

    public static void setEnchantmentAcceptingNew(boolean value) {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Player getAttackerInDamageEvent(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            return (Player) e.getDamager();
        } else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player) {
            return (Player) ((Projectile) e.getDamager()).getShooter();
        } else {
            return null;
        }
    }

    public static LivingEntity getLivingAttackerInDamageEvent(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof LivingEntity) {
            return (LivingEntity) e.getDamager();
        } else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof LivingEntity) {
            return (LivingEntity) ((Projectile) e.getDamager()).getShooter();
        } else {
            return null;
        }
    }

    public static List<ItemStack> getAffectItemInClickEvent(InventoryClickEvent e) {
        List<ItemStack> list = Lists.newArrayList();
        if (e.getClick() == ClickType.NUMBER_KEY) {
            Optional.ofNullable(e.getWhoClicked().getInventory().getItem(e.getHotbarButton())).ifPresent(list::add);
        }
        Optional.ofNullable(e.getCurrentItem()).ifPresent(list::add);
        return list;
    }
}

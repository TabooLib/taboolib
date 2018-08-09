package me.skymc.taboolib.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExpressionType;
import me.skymc.taboolib.skript.expression.ExpressionItemCache;
import me.skymc.taboolib.skript.expression.ExpressionTabooCodeItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 * @author sky
 * @since 2018-02-28 15:40:55
 */
public class SkriptHandler {

    public static void register() {
        if (Bukkit.getPluginManager().getPlugin("Skript") != null) {
            try {
                Skript.registerExpression(ExpressionItemCache.class, ItemStack.class, ExpressionType.SIMPLE, "taboolib itemcache %string%");
                Skript.registerExpression(ExpressionTabooCodeItem.class, ItemStack.class, ExpressionType.SIMPLE, "taboocode itemcache %string%");
            } catch (Exception ignored) {
            }
        }
    }
}

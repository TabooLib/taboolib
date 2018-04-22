package me.skymc.taboolib.skript;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExpressionType;
import me.skymc.taboolib.skript.expression.ExpressionItemCache;
import me.skymc.taboolib.skript.expression.ExpressionTabooCodeItem;

/**
 * @author sky
 * @since 2018-02-28 15:40:55
 */
public class SkriptHandler {
	
	private static SkriptHandler inst = null;
	
	private SkriptHandler() {
		if (Bukkit.getPluginManager().getPlugin("Skript") != null) {
			Skript.registerExpression(ExpressionItemCache.class, ItemStack.class, ExpressionType.SIMPLE, "taboolib itemcache %string%");
			Skript.registerExpression(ExpressionTabooCodeItem.class, ItemStack.class, ExpressionType.SIMPLE, "taboocode itemcache %string%");
		}
	}
	
	public static SkriptHandler getInst() {
		if (inst == null) {
			synchronized (SkriptHandler.class) {
				if (inst == null) {
					inst = new SkriptHandler();
				}
			}
		}
		return inst;
	}
}

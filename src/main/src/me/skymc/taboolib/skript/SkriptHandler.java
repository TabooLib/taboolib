package me.skymc.taboolib.skript;

import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExpressionType;
import me.skymc.taboolib.skript.expression.ExpressionItemCache;

/**
 * @author sky
 * @since 2018-02-28 15:40:55
 */
public class SkriptHandler {
	
	private static SkriptHandler inst = null;
	
	private SkriptHandler() {
		Skript.registerExpression(ExpressionItemCache.class, ItemStack.class, ExpressionType.SIMPLE, "taboolib itemcache %string%");
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

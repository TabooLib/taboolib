package me.skymc.taboolib.skript.expression;

import java.lang.reflect.Method;

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author sky
 * @since 2018-02-28 15:45:49
 */
public class ExpressionTabooCodeItem extends SimpleExpression<ItemStack> {
	
	private Expression<String> name;
	private Class<?> tabooCodeItem;
	private Method getItem;

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] args, int arg1, Kleenean arg2, ParseResult arg3) {
		name = (Expression<String>) args[0];
		try {
			tabooCodeItem = Class.forName("me.skymc.taboocode.TabooCodeItem");
			getItem = tabooCodeItem.getMethod("getItem", String.class, Boolean.class);
		}
		catch (Exception e) {
			//
		}
		return true;
	}

	@Override
	public String toString(Event e, boolean arg1) {
		return this.getClass().getName();
	}

	@Override
	protected ItemStack[] get(Event e) {
		try {
			ItemStack item = (ItemStack) getItem.invoke(null, name.getSingle(e), false);
			return new ItemStack[] { item == null ? null : item.clone() };
		}
		catch (Exception err) {
			return null;
		}
	}
}

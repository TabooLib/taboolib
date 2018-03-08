package me.skymc.taboolib.string.language2;

import java.util.List;

import org.bukkit.entity.Player;

/**
 * @author sky
 * @since 2018-03-08 23:36:22
 */
public abstract interface Language2Line {
	
	abstract void send(Player player);
	
	abstract void console();

}

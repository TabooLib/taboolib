package com.ilummc.tlib.resources;

import org.bukkit.command.CommandSender;

public interface TLocaleSendable {
	
	public static TLocaleSendable getEmpty(String path) {
		return new TLocaleSendable() {
			
			@Override
			public void sendTo(CommandSender sender, String... args) {
				sender.sendMessage("§4<" + path + "§4>");
			}
			
			@Override
			public String asString(String... args) {
				return "§4<" + path + "§4>";
			}
		};
	}

    void sendTo(CommandSender sender, String... args);
    
    String asString(String... args);
}

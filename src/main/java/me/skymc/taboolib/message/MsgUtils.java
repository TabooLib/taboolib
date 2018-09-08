package me.skymc.taboolib.message;

import me.skymc.taboolib.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

@Deprecated
public class MsgUtils {

    public static void send(CommandSender sender, String s) {
        sender.sendMessage(Main.getPrefix() + s.replaceAll("&", "§"));
    }

    public static void send(org.bukkit.entity.Player player, String s) {
        player.sendMessage(Main.getPrefix() + s.replaceAll("&", "§"));
    }

    public static void send(String s) {
        Bukkit.getConsoleSender().sendMessage(Main.getPrefix() + s.replaceAll("&", "§"));
    }

    public static void warn(String s) {
        warn(s, Main.getInst());
    }

    public static void send(String s, Plugin plugin) {
        Bukkit.getConsoleSender().sendMessage("§8[§3" + plugin.getName() + "§8] §7" + s.replaceAll("&", "§"));
    }

    public static void warn(String s, Plugin plugin) {
        Bukkit.getConsoleSender().sendMessage("§4[§c" + plugin.getName() + "§4][WARN #!] §c" + s.replaceAll("&", "§"));
    }

    @Deprecated
    public static void Console(String s) {
        Bukkit.getConsoleSender().sendMessage(Main.getPrefix() + s.replaceAll("&", "§"));
    }

    @Deprecated
    public static void System(String s) {
        System.out.println("[TabooLib] " + s);
    }

    @Deprecated
    public static void Sender(CommandSender p, String s) {
        p.sendMessage(Main.getPrefix() + s.replaceAll("&", "§"));
    }

    @Deprecated
    public static void Player(org.bukkit.entity.Player p, String s) {
        p.sendMessage(Main.getPrefix() + s.replaceAll("&", "§"));
    }

    @Deprecated
    public static String noPe() {
        String s = Main.getInst().getConfig().getString("NO-PERMISSION-MESSAGE").replaceAll("&", "§");
        if ("".equals(s)) {
            s = "§cCONFIG ERROR §8(NO-PERMISSION-MESSAGE)";
        }
        return s;
    }

    @Deprecated
    public static String noClaim(String a) {
        String s = Main.getInst().getConfig().getString("NO-CLAIM-MESSAGE").replaceAll("&", "§").replaceAll("%s%", a);
        if ("".equals(s)) {
            s = "§cCONFIG ERROR §8(NO-CLAIM-MESSAGE)";
        }
        return s;
    }

}

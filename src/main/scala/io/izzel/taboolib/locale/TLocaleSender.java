package io.izzel.taboolib.locale;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @Author sky
 * @Since 2018-05-12 13:58
 */
public interface TLocaleSender {

    void sendTo(CommandSender sender, String... args);

    String asString(String... args);

    List<String> asStringList(String... args);

}

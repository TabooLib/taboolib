package io.izzel.taboolib.module.locale;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @author sky
 * @since 2018-05-12 13:58
 */
public interface TLocaleSender {

    void sendTo(CommandSender sender, String... args);

    String asString(String... args);

    List<String> asStringList(String... args);

}

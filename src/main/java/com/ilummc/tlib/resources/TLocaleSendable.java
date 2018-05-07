package com.ilummc.tlib.resources;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public interface TLocaleSendable {

    static TLocaleSendable getEmpty() {
        return (sender, args) -> {
            // Empty
        };
    }

    static TLocaleSendable getEmpty(String path) {
        return new TLocaleSendable() {

            @Override
            public void sendTo(CommandSender sender, String... args) {
                sender.sendMessage("§4<" + path + "§4>");
            }

            @Override
            public String asString(String... args) {
                return "§4<" + path + "§4>";
            }

            @Override
            public List<String> asStringList(String... args) {
                return Collections.singletonList("§4<" + path + "§4>");
            }
        };
    }

    void sendTo(CommandSender sender, String... args);

    default String asString(String... args) {
        return "";
    }

    default List<String> asStringList(String... args) {
        return Collections.emptyList();
    }
}

package com.ilummc.tlib.resources;

import org.bukkit.command.CommandSender;

public interface Sendable {

    Sendable EMPTY = new Sendable() {
        @Override
        public void sendTo(CommandSender sender) {
        }

        @Override
        public void sendTo(CommandSender sender, String... args) {
        }
    };

    void sendTo(CommandSender sender);

    void sendTo(CommandSender sender, String... args);

}

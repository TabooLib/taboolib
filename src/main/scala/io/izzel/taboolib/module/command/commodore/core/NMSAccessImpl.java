package io.izzel.taboolib.module.command.commodore.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.v1_13_R2.ArgumentRegistry;
import net.minecraft.server.v1_13_R2.CommandListenerWrapper;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.command.BukkitCommandWrapper;

import java.util.Objects;

public class NMSAccessImpl extends NMSAccess{

    @SuppressWarnings("deprecation")
    @Override
    public CommandDispatcher<?> getDispatcher() {
        return MinecraftServer.getServer().getCommandDispatcher().a();
    }

    @Override
    public CommandSender getBukkitSender(Object commandWrapperListener) {
        Objects.requireNonNull(commandWrapperListener, "commandWrapperListener不能为空");
        return ((CommandListenerWrapper)commandWrapperListener).getBukkitSender();
    }

    @Override
    public SuggestionProvider<?> getWrapper(Command command) {
        return new BukkitCommandWrapper((CraftServer) Bukkit.getServer(),command);
    }

    @Override
    public Class<?> getArgumentRegistryClass() {
        return ArgumentRegistry.class;
    }

    @Override
    public Class<?> getMinecraftKeyClass() {
        return MinecraftKey.class;
    }

    @Override
    public Object createMinecraftKey(NamespacedKey key) {
        return new MinecraftKey(key.getNamespace(),key.getKey());
    }
}

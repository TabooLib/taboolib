package io.izzel.taboolib.module.command.commodore.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.izzel.taboolib.module.inject.TInject;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class NMSAccess {
    @TInject(asm = "io.izzel.taboolib.module.command.commodore.core.NMSAccessImpl")
    public static NMSAccess INSTANCE;

    public abstract CommandDispatcher<?> getDispatcher();

    public abstract CommandSender getBukkitSender(Object commandWrapperListener);

    public abstract SuggestionProvider<?> getWrapper(Command command);

    public abstract Class<?> getArgumentRegistryClass();

    public abstract Class<?> getMinecraftKeyClass();

    public abstract Object createMinecraftKey(NamespacedKey key);
}

/*
 * This file is part of commodore, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package io.izzel.taboolib.module.command.commodore.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CommodoreImpl extends Commodore {

    // ArgumentCommandNode＃customSuggestions字段
    private static final Field CUSTOM_SUGGESTIONS_FIELD;

    // CommandNode＃children，CommandNode＃literals，CommandNode＃arguments字段
    private static final Field CHILDREN_FIELD;
    protected static Field LITERALS_FIELD;
    protected static Field ARGUMENTS_FIELD;

    // 上面的CommandNode字段的数组：[＃children，＃literals，＃arguments]
    protected static Field[] CHILDREN_FIELDS;

    static {
        try {
            CUSTOM_SUGGESTIONS_FIELD = ArgumentCommandNode.class.getDeclaredField("customSuggestions");
            CUSTOM_SUGGESTIONS_FIELD.setAccessible(true);

            CHILDREN_FIELD = CommandNode.class.getDeclaredField("children");
            LITERALS_FIELD = CommandNode.class.getDeclaredField("literals");
            ARGUMENTS_FIELD = CommandNode.class.getDeclaredField("arguments");
            CHILDREN_FIELDS = new Field[]{CHILDREN_FIELD, LITERALS_FIELD, ARGUMENTS_FIELD};
            for (Field field : CHILDREN_FIELDS) {
                field.setAccessible(true);
            }
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    protected Plugin plugin;
    protected final List<LiteralCommandNode<?>> registeredNodes = new ArrayList<>();

    public CommodoreImpl(Plugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(new ServerReloadListener(), this.plugin);
        ensureSetup();
    }

    @Override
    public CommandDispatcher<?> getDispatcher() {
        return NMSAccess.INSTANCE.getDispatcher();
    }

    @Override
    public CommandSender getBukkitSender(Object commandWrapperListener) {
        return NMSAccess.INSTANCE.getBukkitSender(commandWrapperListener);
    }

    @Override
    public List<LiteralCommandNode<?>> getRegisteredNodes() {
        return Collections.unmodifiableList(this.registeredNodes);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void register(LiteralCommandNode<?> node) {
        Objects.requireNonNull(node, "node");

        CommandDispatcher dispatcher = getDispatcher();
        RootCommandNode root = dispatcher.getRoot();

        removeChild(root, node.getName());
        root.addChild(node);
        this.registeredNodes.add(node);
    }

    @Override
    public void register(Command command, LiteralCommandNode<?> node, Predicate<? super Player> permissionTest) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(permissionTest, "permissionTest");

        try {
            SuggestionProvider<?> wrapper = NMSAccess.INSTANCE.getWrapper(command);
            setCustomSuggestionProvider(node, wrapper);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Collection<String> aliases = getAliases(command);
        if (!aliases.contains(node.getLiteral())) {
            node = renameLiteralNode(node, command.getName());
        }

        for (String alias : aliases) {
            if (node.getLiteral().equals(alias)) {
                register(node);
            } else {
                register(LiteralArgumentBuilder.literal(alias).redirect((LiteralCommandNode<Object>)node).build());
            }
        }

        Bukkit.getPluginManager().registerEvents(new CommandDataSendListener(command, permissionTest), this.plugin);
    }

    @Override
    public void register(Command command, LiteralCommandNode<?> node) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(node, "node");

        register(command, node, command::testPermissionSilent);
    }

    public static void ensureSetup() {

    }



    /**
     * 获取给定命令的所有已知别名。
     *
     * <p>这将包括主标签，已定义的别名以及包括Bukkit添加的后备前缀的别名。</p>
     *
     * @param command 命令
     * @return 别名
     */
    protected static Collection<String> getAliases(Command command) {
        Objects.requireNonNull(command, "command");

        Stream<String> aliasesStream = Stream.concat(
                Stream.of(command.getLabel()),
                command.getAliases().stream()
        );

        if (command instanceof PluginCommand) {
            String fallbackPrefix = ((PluginCommand) command).getPlugin().getName().toLowerCase().trim();
            aliasesStream = aliasesStream.flatMap(alias -> Stream.of(
                    alias,
                    fallbackPrefix + ":" + alias
            ));
        }

        return aliasesStream.distinct().collect(Collectors.toList());
    }

    protected static void setCustomSuggestionProvider(CommandNode<?> node, SuggestionProvider<?> suggestionProvider) {
        if (node instanceof ArgumentCommandNode) {
            ArgumentCommandNode<?, ?> argumentNode = (ArgumentCommandNode<?, ?>) node;
            try {
                CUSTOM_SUGGESTIONS_FIELD.set(argumentNode, suggestionProvider);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        // 递归应用于子节点
        for (CommandNode<?> child : node.getChildren()) {
            setCustomSuggestionProvider(child, suggestionProvider);
        }
    }

    protected static <S> LiteralCommandNode<S> renameLiteralNode(LiteralCommandNode<S> node, String newLiteral) {
        LiteralCommandNode<S> clone = new LiteralCommandNode<>(newLiteral, node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());
        for (CommandNode<S> child : node.getChildren()) {
            clone.addChild(child);
        }
        return clone;
    }



    /**
     * 删除minecraft命名空间的参数数据，以及未经许可查看以下内容的玩家数据
     * 相应的命令。
     */
    protected static final class CommandDataSendListener implements Listener {
        private final Set<String> aliases;
        private final Set<String> minecraftPrefixedAliases;
        private final Predicate<? super Player> permissionTest;

        CommandDataSendListener(Command pluginCommand, Predicate<? super Player> permissionTest) {
            this.aliases = new HashSet<>(getAliases(pluginCommand));
            this.minecraftPrefixedAliases = this.aliases.stream().map(alias -> "minecraft:" + alias).collect(Collectors.toSet());
            this.permissionTest = permissionTest;
        }

        @EventHandler
        public void onCommandSend(PlayerCommandSendEvent e) {
            // 始终删除craftbukkit添加的带有前缀的"minecraft:"前缀。
            // 发生这种情况是因为bukkit认为我们注入的命令是普通命令。
            e.getCommands().removeAll(this.minecraftPrefixedAliases);

            // 如果玩家未通过权限测试，则删除实际的别名
            if (!this.permissionTest.test(e.getPlayer())) {
                e.getCommands().removeAll(this.aliases);
            }
        }
    }

    /**
     * 监听服务器（重新）加载，并将所有已注册的节点重新添加到调度程序。
     */
    protected final class ServerReloadListener implements Listener {
        @SuppressWarnings("rawtypes")
        @EventHandler
        public void onLoad(ServerLoadEvent e) {
            CommandDispatcher dispatcher = getDispatcher();
            RootCommandNode root = dispatcher.getRoot();

            for (LiteralCommandNode<?> node : CommodoreImpl.this.registeredNodes) {
                removeChild(root, node.getName());
                root.addChild(node);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    protected static void removeChild(RootCommandNode root, String name) {
        try {
            for (Field field : CHILDREN_FIELDS) {
                Map<String, ?> children = (Map<String, ?>) field.get(root);
                children.remove(name);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}

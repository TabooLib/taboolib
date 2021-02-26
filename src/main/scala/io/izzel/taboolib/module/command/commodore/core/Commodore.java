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
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 在Bukkit插件中使用Minecraft的1.13"brigadier"库的实用工具。
 */
public abstract class Commodore {

    /**
     * 获取当前命令调度程序实例。
     *
     * <p>CraftBukkit在整个服务器运行期间不使用相同的调度程序实例。
     * 每次加载新插件时，调度程序实例都会被完全擦除（并替换为新实例）。</p>
     *
     * @return 命令调度器
     */
    public abstract CommandDispatcher<?> getDispatcher();

    /**
     * 获取与传递的CommandWrapperListener关联的CommandSender。
     *
     * <p>Minecraft使用CommandWrapperListener实例调用Brigadier命令处理程序，
     * 如果不访问nms代码则无法访问该实例。 该方法将一个Object作为参数，但是实际接
     * 受的唯一类型是Minecraft提供的S类型的类。</p>
     *
     * @param commandWrapperListener 来自nms的CommandWrapperListener实例。
     * @return 包装为CommandSender的CommandWrapperListener。
     */
    public abstract CommandSender getBukkitSender(Object commandWrapperListener);

    /**
     * 获取此实例注册到{@link CommandDispatcher}的所有节点的列表。
     *
     * @return 所有已注册节点的列表。
     */
    public abstract List<LiteralCommandNode<?>> getRegisteredNodes();

    /**
     * 根据为{@code command}定义的所有别名，将提供的参数数据注册到调度程序。
     *
     * <p>另外，将CraftBukkit {@link SuggestionProvider}应用于节点内的所有参数，
     * 因此ASK_SERVER建议可以继续对该命令起作用。</p>
     *
     * <p>只有玩家通过 {@code PermissionTest}，才会向其发送参数数据.</p>
     *
     * @param command        从中读取别名的命令
     * @param node           参数数据
     * @param permissionTest Predicate，检查是否应向玩家发送参数数据
     */
    public abstract void register(Command command, LiteralCommandNode<?> node, Predicate<? super Player> permissionTest);

    /**
     * 根据为{@code command}定义的所有别名，将提供的参数数据注册到调度程序。
     *
     * <p>另外，将CraftBukkit {@link SuggestionProvider}应用于节点内的所有参数，
     * 因此ASK_SERVER建议可以继续对该命令起作用。</p>
     *
     * <p>只有玩家通过 {@code PermissionTest}，才会向其发送参数数据.</p>
     *
     * @param command         从中读取别名的命令
     * @param argumentBuilder 构造器形式的参数数据
     * @param permissionTest  Predicate，检查是否应向玩家发送参数数据
     */
    public void register(Command command, LiteralArgumentBuilder<?> argumentBuilder, Predicate<? super Player> permissionTest) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(argumentBuilder, "argumentBuilder");
        Objects.requireNonNull(permissionTest, "permissionTest");
        register(command, argumentBuilder.build(), permissionTest);
    }

    /**
     * 根据为{@code command}定义的所有别名，将提供的参数数据注册到调度程序。
     *
     * <p>另外，将CraftBukkit {@link SuggestionProvider}应用于节点内的所有参数，
     * 因此ASK_SERVER建议可以继续对该命令起作用。</p>
     *
     * @param command 从中读取别名的命令
     * @param node    参数数据
     */
    public abstract void register(Command command, LiteralCommandNode<?> node);

    /**
     * 根据为{@code command}定义的所有别名，将提供的参数数据注册到调度程序。
     *
     * <p>另外，将CraftBukkit {@link SuggestionProvider}应用于节点内的所有参数，
     * 因此ASK_SERVER建议可以继续对该命令起作用。</p>
     *
     * @param command         从中读取别名的命令
     * @param argumentBuilder 构造器形式的参数数据
     */
    public void register(Command command, LiteralArgumentBuilder<?> argumentBuilder) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(argumentBuilder, "argumentBuilder");
        register(command, argumentBuilder.build());
    }

    /**
     * 将提供的参数数据注册到调度程序。
     *
     * <p>等效于调用
     * {@link CommandDispatcher#register(LiteralArgumentBuilder)}。</p>
     *
     * <p>建议使用 {@link #register(Command, LiteralCommandNode)}.</p>
     *
     * @param node 参数数据
     */
    public abstract void register(LiteralCommandNode<?> node);

    /**
     * 将提供的参数数据注册到调度程序。
     *
     * <p>等效于调用
     * {@link CommandDispatcher#register(LiteralArgumentBuilder)}.</p>
     *
     * <p>建议使用 {@link #register(Command, LiteralArgumentBuilder)}.</p>
     *
     * @param argumentBuilder 参数数据
     */
    public void register(LiteralArgumentBuilder<?> argumentBuilder) {
        Objects.requireNonNull(argumentBuilder, "argumentBuilder");
        register(argumentBuilder.build());
    }

}

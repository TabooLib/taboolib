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

package io.izzel.taboolib.module.command.commodore;

import io.izzel.taboolib.module.command.commodore.core.BrigadierUnsupportedException;
import io.izzel.taboolib.module.command.commodore.core.Commodore;
import io.izzel.taboolib.module.command.commodore.core.CommodoreImpl;
import io.izzel.taboolib.module.command.commodore.core.MinecraftArgumentTypes;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 用于获取{@link Commodore}实例的工厂。
 */
public final class CommodoreProvider {
    private CommodoreProvider() { throw new AssertionError(); }
    private static Map<Plugin,Commodore> pluginCommodoreMap = new HashMap<>();

    private static final Throwable SETUP_EXCEPTION = checkSupported();

    private static Throwable checkSupported() {
        try {
            Class.forName("com.mojang.brigadier.CommandDispatcher");
            CommodoreImpl.ensureSetup();
            MinecraftArgumentTypes.ensureSetup();
            return null;
        } catch (Throwable e) {
            return e;
        }
    }

    /**
     * 检查服务器是否支持Brigadier命令系统。
     *
     * @return 如果支持commodore，则为true。
     */
    public static boolean isSupported() {
        return SETUP_EXCEPTION == null;
    }

    /**
     * 获取给定插件的{@link Commodore}实例。
     *
     * @param plugin 插件
     * @return Brigadier实例
     * @throws BrigadierUnsupportedException 如果服务器不 {@link #isSupported() 支持} Brigadier。
     */
    public static Commodore getCommodore(Plugin plugin) throws BrigadierUnsupportedException {
        if(pluginCommodoreMap.containsKey(plugin)){
            return pluginCommodoreMap.get(plugin);
        }
        Objects.requireNonNull(plugin, "plugin");
        if (SETUP_EXCEPTION != null) {
            throw new BrigadierUnsupportedException("Brigadier is not supported by the server.", SETUP_EXCEPTION);
        }
        CommodoreImpl commodore = new CommodoreImpl(plugin);
        pluginCommodoreMap.put(plugin,commodore);
        return commodore;
    }
}

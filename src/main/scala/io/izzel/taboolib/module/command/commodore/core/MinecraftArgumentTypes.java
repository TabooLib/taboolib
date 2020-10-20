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

import com.mojang.brigadier.arguments.ArgumentType;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Minecraft提供的{@link ArgumentType}的注册表。
 */
public final class MinecraftArgumentTypes {
    private MinecraftArgumentTypes() {}

    // ArgumentRegistry#getByKey（混淆）方法
    // 私有构造函数，请勿修改
    private static final Method ARGUMENT_REGISTRY_GET_BY_KEY_METHOD;

    // ArgumentRegistry.Entry#clazz（混淆）字段
    private static final Field ARGUMENT_REGISTRY_ENTRY_CLASS_FIELD;

    static {
        try {
            ARGUMENT_REGISTRY_GET_BY_KEY_METHOD = NMSAccess.INSTANCE.getArgumentRegistryClass().getDeclaredMethod("a",NMSAccess.INSTANCE.getMinecraftKeyClass());
            ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.setAccessible(true);

            Class<?> argumentRegistryEntry = ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.getReturnType();
            ARGUMENT_REGISTRY_ENTRY_CLASS_FIELD = argumentRegistryEntry.getDeclaredField("a");
            ARGUMENT_REGISTRY_ENTRY_CLASS_FIELD.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * 获取是否为给定键注册了参数。
     *
     * @param key 键
     * @return 参数是否已注册
     */
    public static boolean isRegistered(NamespacedKey key) {
        try {
            Object minecraftKey = NMSAccess.INSTANCE.createMinecraftKey(key);
            Object entry = ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.invoke(null, minecraftKey);
            return entry != null;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过键获取注册的参数类型类。
     *
     * @param key 键
     * @return 返回的参数类型类
     * @throws IllegalArgumentException 如果没有注册此类参数，则抛出
     */
    public static Class<? extends ArgumentType<?>> getClassByKey(NamespacedKey key) throws IllegalArgumentException {
        try {
            Object minecraftKey = NMSAccess.INSTANCE.createMinecraftKey(key);
            Object entry = ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.invoke(null, minecraftKey);
            if (entry == null) {
                throw new IllegalArgumentException(key.toString());
            }

            final Class<?> argument = (Class<?>) ARGUMENT_REGISTRY_ENTRY_CLASS_FIELD.get(entry);
            return (Class<? extends ArgumentType<?>>) argument;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过键获取注册的参数类型。
     *
     * @param key 键
     * @return 返回的参数
     * @throws IllegalArgumentException 如果没有注册此类参数，则抛出
     */
    public static ArgumentType<?> getByKey(NamespacedKey key) throws IllegalArgumentException {
        try {
            final Constructor<? extends ArgumentType<?>> constructor = getClassByKey(key).getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ensureSetup() {
        // do nothing - 仅被调用以触发静态初始化程序
    }
}

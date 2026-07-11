package taboolib.module.nms;

import com.mojang.authlib.GameProfile;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import taboolib.common.reflect.ClassHelper;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * MeteorInjector
 *
 * 不依赖监听器的数据包注入实现。
 * 项目原名称为 PacketListener，来自：Meteor2333/PacketListener
 * 原项目多个文件整合为单独的 MeteorInjector 作为实现
 * 部分代码来自 LightInjector：frengor/LightInjector
 *
 * @author Meteor23333
 * @author fren_gor
 */
@SuppressWarnings({"SameParameterValue", "unused", "StatementWithEmptyBody", "LoopConditionNotUpdatedInsideLoop", "unchecked", "JavadocBlankLines"})
public class MeteorInjector implements Closeable {

    private static final Class<?> SERVER_CLASS = getNMSClass("MinecraftServer", "server", "MinecraftServer", "server");
    private static final Class<?> SERVER_CONNECTION_CLASS = getNMSClass("ServerConnection", "server.network", "ServerConnectionListener", "server.network");
    private static Class<?> PACKET_LOGIN_OUT_SUCCESS_CLASS;

    private static final Field NMS_SERVER = getField(getCBClass("CraftServer"), SERVER_CLASS, 1);
    private static final Field NMS_SERVER_CONNECTION = getField(SERVER_CLASS, SERVER_CONNECTION_CLASS, 1);
    private static Field GAME_PROFILE_FROM_PACKET;
    private static final Field CHANNELS_LIST = getField(SERVER_CONNECTION_CLASS, List.class, 1);

    private static final Method GAME_PROFILE_ID = getMethod(GameProfile.class, MinecraftVersion.INSTANCE.getVersionId() > 12108 ? "id" : "getId");

    private static final String IDENTIFIER_PREFIX = "meteor-injector-";

    private final Plugin plugin;
    private final String identifier;
    private static int ID = 0;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final Thread thread;
    private final Listener serverLoadListener;
    // Netty channels maintained by ServerConnection; used to attach interceptors as soon as they appear.
    private final List<ChannelFuture> channels;
    // Track injected player channels to cleanly remove the handler on shutdown.
    private final Set<Channel> injectedChannels = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    /**
     * Creates and starts a background thread that waits for the server channel to be ready,
     * then injects a pipeline hook for new connections.
     */
    public MeteorInjector(@NotNull Plugin plugin) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("MeteorInjector must be constructed on the main thread.");
        }
        if (!Objects.requireNonNull(plugin, "Plugin is null.").isEnabled()) {
            throw new IllegalArgumentException("Plugin " + plugin.getName() + " is not enabled");
        }

        this.plugin = plugin;
        this.identifier = Objects.requireNonNull(getIdentifier(), "getIdentifier() returned a null value.") + '-' + ID++;

        Object conn;
        try {
            // PacketLoginOutSuccess, ClientboundLoginFinishedPacket, ClientboundGameProfilePacket
            try {
                PACKET_LOGIN_OUT_SUCCESS_CLASS = getNMSClass("PacketLoginOutSuccess", "network.protocol.login", "ClientboundLoginFinishedPacket", "network.protocol.login");
            } catch (Throwable ignored) {
                PACKET_LOGIN_OUT_SUCCESS_CLASS = getNMSClass("PacketLoginOutSuccess", "network.protocol.login", "ClientboundGameProfilePacket", "network.protocol.login");
            }
            GAME_PROFILE_FROM_PACKET = getField(PACKET_LOGIN_OUT_SUCCESS_CLASS, GameProfile.class, 1);

            conn = NMS_SERVER_CONNECTION.get(NMS_SERVER.get(Bukkit.getServer()));
            channels = (List<ChannelFuture>) CHANNELS_LIST.get(conn);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("[MeteorInjector] An error occurred while injecting.", exception);
        }

        if (conn == null) {
            throw new RuntimeException("[MeteorInjector] ServerConnection is null."); // Should never happen
        }
        if (channels == null) {
            throw new RuntimeException("[MeteorInjector] List<ChannelFuture> is null."); // Should never happen
        }

        thread = new Thread(() -> {

            // Block until Channels is not empty.
            // 一般来说开服后就会注入上，无需担心忙等待问题
            while (channels.isEmpty());

            if (isClosed()) return;
            injectParentHandlers();

            Thread.yield();
        }, identifier);
        thread.setDaemon(true);
        thread.start();

        // ServerLoadEvent 后补扫一次，捕获插件启用阶段尚未注册的本地通道。
        serverLoadListener = new Listener() {

            @EventHandler(priority = EventPriority.MONITOR)
            public void onServerLoad(ServerLoadEvent event) {
                HandlerList.unregisterAll(this);
                injectParentHandlers();
            }
        };
        Bukkit.getPluginManager().registerEvents(serverLoadListener, plugin);
    }

    /**
     * 为尚未被任意 MeteorInjector 覆盖的服务端父通道添加监听器。
     */
    private void injectParentHandlers() {
        if (isClosed()) return;
        synchronized (channels) {
            // 双重检查：close() 在进入此同步块之前已置位 closed，此处防止延迟扫描与 close() 竞争时重复注入。
            if (isClosed()) return;
            // Typically, Channels on the server side contain only one element, but this is done just to be safe.
            for (ChannelFuture channel : channels) {
                ChannelPipeline pipeline = channel.channel().pipeline();
                if (pipeline.names().stream().anyMatch(name -> name.startsWith(IDENTIFIER_PREFIX))) {
                    continue;
                }
                pipeline.addFirst(identifier, new ChannelInboundHandlerAdapter() {

                    @Override
                    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                        try {
                            if (o instanceof Channel) {
                                Channel ch = (Channel) o;
                                new NettyPipelineInjector(ch.pipeline());
                            }
                        } finally {
                            super.channelRead(channelHandlerContext, o);
                        }
                    }
                });
            }
        }
    }

    /**
     * Dispatches inbound packets to ProtocolHandler, supporting both player and handshake stages.
     */
    public @Nullable Object onPacketReceive(@Nullable Player sender, @NotNull Channel channel, @NotNull Object packet) {
        if (sender != null) {
            PacketReceiveEvent event = new PacketReceiveEvent(sender, new PacketImpl(packet));
            if (event.callIf()) {
                return ProtocolHandler.INSTANCE.handlePacket(ProtocolHandler.PACKET_RECEIVE, sender, channel, event.getPacket().getSource());
            } else {
                return null;
            }
        } else {
            PacketReceiveEvent.Handshake event = new PacketReceiveEvent.Handshake(channel, new PacketImpl(packet));
            if (event.callIf()) {
                return ProtocolHandler.INSTANCE.handlePacket(ProtocolHandler.PACKET_RECEIVE, null, channel, event.getPacket().getSource());
            } else {
                return null;
            }
        }
    }

    /**
     * Dispatches outbound packets to ProtocolHandler, supporting both player and handshake stages.
     */
    public @Nullable Object onPacketSend(@Nullable Player receiver, @NotNull Channel channel, @NotNull Object packet) {
        if (receiver != null) {
            PacketSendEvent event = new PacketSendEvent(receiver, new PacketImpl(packet));
            if (event.callIf()) {
                return ProtocolHandler.INSTANCE.handlePacket(ProtocolHandler.PACKET_SEND, receiver, channel, event.getPacket().getSource());
            } else {
                return null;
            }
        } else {
            PacketSendEvent.Handshake event = new PacketSendEvent.Handshake(channel, new PacketImpl(packet));
            if (event.callIf()) {
                return ProtocolHandler.INSTANCE.handlePacket(ProtocolHandler.PACKET_SEND, null, channel, event.getPacket().getSource());
            } else {
                return null;
            }
        }
    }

    protected @NotNull String getIdentifier() {
        return IDENTIFIER_PREFIX + plugin.getName();
    }

    public final boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() throws IOException {
        if (closed.getAndSet(true)) {
            return;
        }

        thread.interrupt();
        HandlerList.unregisterAll(serverLoadListener);
        synchronized (channels) {
            for (ChannelFuture channel : channels) {
                ChannelPipeline pipeline = channel.channel().pipeline();
                if (pipeline.get(identifier) != null) {
                    pipeline.remove(identifier);
                }
            }
        }
        for (Channel channel : injectedChannels) {
            ChannelPipeline pipeline = channel.pipeline();
            if (pipeline.get(identifier) != null) {
                pipeline.remove(identifier);
            }
        }
        injectedChannels.clear();
    }

    /**
     * Used to inject the packet interceptor into a {@link ChannelPipeline} at the appropriate time.
     * 用于在合适的时机将 PacketInterceptor 包拦截器注入到 ChannelPipeline
     */
    private final class NettyPipelineInjector {

        public NettyPipelineInjector(@NotNull ChannelPipeline pipeline) {
            pipeline.addLast(new ChannelInboundHandlerAdapter() {

                @Override
                public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
                    try {
                        // Add the interceptor before the packet is handled.
                        // 在处理数据包之前添加拦截器 PacketInterceptor
                        channelHandlerContext.pipeline().addBefore(
                                "packet_handler",
                                identifier,
                                new PacketInterceptor()
                        );
                        injectedChannels.add(channelHandlerContext.channel());
                    } finally {
                        super.channelActive(channelHandlerContext);
                    }
                }
            });
        }
    }

    /**
     * Packet interceptor. All written or read packets pass through here.
     */
    private class PacketInterceptor extends ChannelDuplexHandler {

        private GameProfile profile;
        private Player player;

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            // Called during player disconnection
            // 在玩家断开连接时调用
            // Clean data structures
            // 清理数据结构
            injectedChannels.remove(ctx.channel());
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            @Nullable Object newPacket;
            try {
                newPacket = onPacketReceive(player, ctx.channel(), msg);
            } catch (OutOfMemoryError error) {
                // Out of memory, re-throw and return immediately
                throw error;
            } catch (Throwable throwable) {
                plugin.getLogger().log(Level.SEVERE, "[MeteorInjector] An error occurred while calling onPacketReceiveAsync:", throwable);
                super.channelRead(ctx, msg);
                return;
            }
            if (newPacket != null)
                super.channelRead(ctx, newPacket);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (PACKET_LOGIN_OUT_SUCCESS_CLASS.isInstance(msg)) {
                try {
                    profile = (GameProfile) GAME_PROFILE_FROM_PACKET.get(msg);
                } catch (ReflectiveOperationException exception) {
                    plugin.getLogger().log(Level.SEVERE, "[MeteorInjector] An error occurred while handling PacketLoginOutSuccess:", exception);
                }
            }
            if (player == null && profile != null) {
                try {
                    player = Bukkit.getPlayer((UUID) GAME_PROFILE_ID.invoke(profile));
                } catch (ReflectiveOperationException exception) {
                    plugin.getLogger().log(Level.SEVERE, "[MeteorInjector] An error occurred while handling PacketLoginOutSuccess:", exception);
                }
            }
            @Nullable Object newPacket;
            try {
                newPacket = onPacketSend(player, ctx.channel(), msg);
            } catch (OutOfMemoryError error) {
                // Out of memory, re-throw and return immediately
                throw error;
            } catch (Throwable throwable) {
                plugin.getLogger().log(Level.SEVERE, "[MeteorInjector] An error occurred while calling onPacketSend:", throwable);
                super.write(ctx, msg, promise);
                return;
            }
            if (newPacket != null)
                super.write(ctx, newPacket, promise);
        }
    }

    // ====================================== Reflection stuff ======================================

    private static Class<?> getNMSClass(String name, String mcPackage, String mojmapName, String mojmapMcPackage) {
        String clazz;
        // NOTICE 从 1.17+ 开始, NMS 不再带有版本号
        if (MinecraftVersion.INSTANCE.isMojangMapping()) {
            clazz = "net.minecraft." + mojmapMcPackage + '.' + mojmapName;
        }
        else if (MinecraftVersion.INSTANCE.isUniversal()) {
            clazz = "net.minecraft." + mcPackage + '.' + name;
        } else {
            clazz = "net.minecraft.server." + MinecraftVersion.INSTANCE.getMinecraftVersion() + '.' + name;
        }
        try {
            return ClassHelper.getClass(clazz);
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException("[MeteorInjector] Cannot find NMS Class! (" + clazz + ')', exception);
        }
    }

    private static Class<?> getCBClass(String name) {
        String version = MinecraftVersion.INSTANCE.getMinecraftVersion();
        String clazz;
        // NOTICE 在 Paper 1.20.6+ 此方法失效，返回 "UNKNOWN"
        if (version.equals("UNKNOWN")) {
            clazz = "org.bukkit.craftbukkit." + name;
        } else {
            clazz = "org.bukkit.craftbukkit." + version + "." + name;
        }
        try {
            return ClassHelper.getClass(clazz);
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException("[MeteorInjector] Cannot find CB Class! (" + clazz + ')', exception);
        }
    }

    private static Field getField(Class<?> clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("[MeteorInjector] Cannot find field! (" + clazz.getName() + '.' + name + ')', exception);
        }
    }

    private static Field getField(Class<?> clazz, Class<?> type, @Range(from = 1, to = Integer.MAX_VALUE) int index) {
        return getField(clazz, type, index, 0);
    }

    private static Field getField(Class<?> clazz, Class<?> type, @Range(from = 1, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Integer.MAX_VALUE) int superClassesToTry) {
        final Class<?> savedClazz = clazz;
        final int savedIndex = index;

        // Try to find the field for superClassesToTry super classes
        for (int i = 0; i <= superClassesToTry; i++) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                if (type.equals(f.getType()) && --index <= 0) {
                    f.setAccessible(true);
                    return f;
                }
            }
            // Didn't find any field, check with isAssignableFrom
            index = savedIndex;
            for (Field f : fields) {
                if (type.isAssignableFrom(f.getType()) && --index <= 0) {
                    f.setAccessible(true);
                    return f;
                }
            }
            // Didn't find any field again, try with super class
            clazz = clazz.getSuperclass();
            if (clazz == null || clazz == Object.class) {
                break; // Don't continue if we arrived at Object
            }
            index = savedIndex; // Reset index before running the loop again
        }

        String errorMsg = "[MeteorInjector] Cannot find field! (" + savedIndex + getOrdinal(savedIndex) + type.getName() + " in " + savedClazz.getName();
        if (superClassesToTry > 0) {
            errorMsg += " and in its " + superClassesToTry + (superClassesToTry == 1 ? " super class" : " super classes");
        }
        errorMsg += ')';

        throw new RuntimeException(errorMsg);
    }

    private static Method getMethod(Class<?> clazz, String name, Class<?>... parameters) {
        try {
            Method m = clazz.getDeclaredMethod(name, parameters);
            m.setAccessible(true);
            return m;
        } catch (ReflectiveOperationException exception) {
            StringJoiner params = new StringJoiner(", ");
            for (Class<?> p : parameters) {
                params.add(p.getName());
            }
            throw new RuntimeException("[MeteorInjector] Cannot find method! (" + clazz.getName() + '.' + name + '(' + params + ')', exception);
        }
    }

    private static Method getMethod(Class<?> clazz, Class<?> returnType, @Range(from = 1, to = Integer.MAX_VALUE) int index) {
        final int savedIndex = index;
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            if (returnType.equals(m.getReturnType()) && --index <= 0) {
                m.setAccessible(true);
                return m;
            }
        }
        // Didn't find any method, check with isAssignableFrom
        index = savedIndex;
        for (Method m : methods) {
            if (returnType.isAssignableFrom(m.getReturnType()) && --index <= 0) {
                m.setAccessible(true);
                return m;
            }
        }

        throw new RuntimeException("[MeteorInjector] Cannot find method! (" + savedIndex + getOrdinal(savedIndex) + " returning " + returnType.getName() + " in " + clazz.getName() + ')');
    }

    // Details are important =P
    private static String getOrdinal(int i) {
        switch (i) {
            case 1:
                return "st ";
            case 2:
                return "nd ";
            case 3:
                return "rd ";
            default:
                return "th ";
        }
    }
}

package taboolib.module.nms;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import taboolib.common.platform.function.ExecutorKt;
import taboolib.common.platform.function.IOKt;
import taboolib.module.nms.TinyReflection.FieldAccessor;
import taboolib.module.nms.TinyReflection.MethodInvoker;
import taboolib.platform.BukkitPlugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a very tiny alternative to ProtocolLib.
 * <p>
 * It now supports intercepting packets during login and status ping (such as OUT_SERVER_PING)!
 * <p>
 * 改了一些东西，支持了 1.20.4 之后的版本
 *
 * @author Kristian, sky
 */
@SuppressWarnings("CallToPrintStackTrace")
public class TinyProtocol {

    private static final AtomicInteger ID = new AtomicInteger(0);

    // Required Minecraft classes
    private static final Class<?> ENTITY_PLAYER_CLASS = TinyReflection.getClass("{nms}.EntityPlayer", "net.minecraft.server.level.EntityPlayer");
    private static final Class<?> PLAYER_CONNECTION_CLASS = TinyReflection.getClass("{nms}.PlayerConnection", "net.minecraft.server.network.PlayerConnection");
    private static final Class<?> NETWORK_MANAGER_CLASS = TinyReflection.getClass("{nms}.NetworkManager", "net.minecraft.network.NetworkManager");

    // Used in order to lookup a channel
    private static final MethodInvoker GET_PLAYER_HANDLE = TinyReflection.getMethod("{obc}.entity.CraftPlayer", "getHandle");
    private static final FieldAccessor<?> GET_CONNECTION = TinyReflection.getField(ENTITY_PLAYER_CLASS, null, PLAYER_CONNECTION_CLASS);
    private static final FieldAccessor<?> GET_MANAGER = TinyReflection.getField(PLAYER_CONNECTION_CLASS, null, NETWORK_MANAGER_CLASS);
    private static final FieldAccessor<Channel> GET_CHANNEL = TinyReflection.getField(NETWORK_MANAGER_CLASS, Channel.class, 0);

    // Looking up ServerConnection
    private static final Class<Object> MINECRAFT_SERVER_CLASS = TinyReflection.getUntypedClass("{nms}.MinecraftServer", "net.minecraft.server.MinecraftServer");
    private static final Class<Object> SERVER_CONNECTION_CLASS = TinyReflection.getUntypedClass("{nms}.ServerConnection", "net.minecraft.server.network.ServerConnection");
    private static final FieldAccessor<Object> GET_MINECRAFT_SERVER = TinyReflection.getField("{obc}.CraftServer", MINECRAFT_SERVER_CLASS, 0);
    private static final FieldAccessor<Object> GET_SERVER_CONNECTION = TinyReflection.getField(MINECRAFT_SERVER_CLASS, SERVER_CONNECTION_CLASS, 0);

    // Packets we have to intercept
    private static final Class<?> PACKET_LOGIN_IN_START = TinyReflection.getClass("{nms}.PacketLoginInStart", "net.minecraft.network.protocol.login.PacketLoginInStart");

    // 从 PacketLoginInStart 中获取玩家名，1.20.4 之后使用 GameProfile
    private static FieldAccessor<String> getName;
    private static FieldAccessor<GameProfile> getGameProfile;

    // Speedup channel lookup
    private final Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
    private Listener listener;

    // Channels that have already been removed
    private final Set<Channel> uninjectedChannels = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    // List of network markers
    private List<Object> networkManagers;

    // Injected channel handlers
    private final List<Channel> serverChannels = new ArrayList<>();
    private ChannelInboundHandlerAdapter serverChannelHandler;
    private ChannelInitializer<Channel> beginInitProtocol;
    private ChannelInitializer<Channel> endInitProtocol;

    // Current handler name
    private final String handlerName;

    protected volatile boolean closed;

    /**
     * Construct a new instance of TinyProtocol, and start intercepting packets for all connected clients and future clients.
     * <p>
     * You can construct multiple instances per plugin.
     */
    public TinyProtocol() {
        // Compute handler name
        this.handlerName = getHandlerName();

        // Initialize reflection fields
        try {
            getName = TinyReflection.getField(PACKET_LOGIN_IN_START, String.class, 0);
        } catch (IllegalArgumentException e) {
            getGameProfile = TinyReflection.getField(PACKET_LOGIN_IN_START, GameProfile.class, 0);
        }

        // Prepare existing players
        registerBukkitEvents();

        try {
            registerChannelHandler();
            registerPlayers();
        } catch (IllegalArgumentException ex) {
            ExecutorKt.runTask(() -> {
                registerChannelHandler();
                registerPlayers();
            });
        }
    }

    private void createServerChannelHandler() {
        // Handle connected channels
        endInitProtocol = new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel channel) {
                try {
                    // This can take a while, so we need to stop the main thread from interfering
                    synchronized (Collections.unmodifiableList(networkManagers)) {
                        // Stop injecting channels
                        if (!closed) {
                            channel.eventLoop().submit(() -> injectChannelInternal(channel));
                        }
                    }
                } catch (Exception e) {
                    IOKt.severe("Cannot inject incoming channel " + channel);
                    e.printStackTrace();
                }
            }
        };

        // This is executed before Minecraft's channel handler
        beginInitProtocol = new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(endInitProtocol);
            }
        };

        serverChannelHandler = new ChannelInboundHandlerAdapter() {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                Channel channel = (Channel) msg;

                // Prepare to initialize ths channel
                channel.pipeline().addFirst(beginInitProtocol);
                ctx.fireChannelRead(msg);
            }
        };
    }

    /**
     * Register bukkit events.
     */
    private void registerBukkitEvents() {
        listener = new Listener() {

            @EventHandler(priority = EventPriority.LOWEST)
            public void onPlayerLogin(PlayerLoginEvent e) {
                if (closed) {
                    return;
                }
                // Don't inject players that have been explicitly uninjected
                if (!uninjectedChannels.contains(getChannel(e.getPlayer()))) {
                    injectPlayer(e.getPlayer());
                }
            }
        };
        Bukkit.getServer().getPluginManager().registerEvents(listener, BukkitPlugin.getInstance());
    }

    @SuppressWarnings("unchecked")
    private void registerChannelHandler() {
        Object mcServer = GET_MINECRAFT_SERVER.get(Bukkit.getServer());
        Object serverConnection = GET_SERVER_CONNECTION.get(mcServer);
        boolean looking = true;

        try {
            Field field = TinyReflection.getParameterizedField(SERVER_CONNECTION_CLASS, List.class, NETWORK_MANAGER_CLASS);
            field.setAccessible(true);
            networkManagers = (List<Object>) field.get(serverConnection);
        } catch (Exception ex) {
            IOKt.warning("Encountered an exception checking list fields");
            ex.printStackTrace();
            MethodInvoker method = TinyReflection.getTypedMethod(SERVER_CONNECTION_CLASS, null, List.class, SERVER_CONNECTION_CLASS);
            networkManagers = (List<Object>) method.invoke(null, serverConnection);
        }
        if (networkManagers == null) {
            throw new IllegalArgumentException("Failed to obtain list of network managers");
        }

        // We need to synchronize against this list
        createServerChannelHandler();

        // Find the correct list, or implicitly throw an exception
        for (int i = 0; looking; i++) {
            List<Object> list = TinyReflection.getField(serverConnection.getClass(), List.class, i).get(serverConnection);
            for (Object item : list) {
                if (!(item instanceof ChannelFuture)) {
                    break;
                }
                // Channel future that contains the server connection
                Channel serverChannel = ((ChannelFuture) item).channel();
                serverChannels.add(serverChannel);
                serverChannel.pipeline().addFirst(serverChannelHandler);
                looking = false;
            }
        }
    }

    private void unregisterChannelHandler() {
        if (serverChannelHandler == null) {
            return;
        }
        for (Channel serverChannel : serverChannels) {
            ChannelPipeline pipeline = serverChannel.pipeline();
            // Remove channel handler
            serverChannel.eventLoop().execute(() -> {
                try {
                    pipeline.remove(serverChannelHandler);
                } catch (NoSuchElementException e) {
                    // That's fine
                }
            });
        }
    }

    private void registerPlayers() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            injectPlayer(player);
        }
    }

    /**
     * Invoked when the server is starting to send a packet to a player.
     * <p>
     * Note that this is not executed on the main thread.
     *
     * @param receiver - the receiving player, NULL for early login/status packets.
     * @param channel  - the channel that received the packet. Never NULL.
     * @param packet   - the packet being sent.
     * @return The packet to send instead, or NULL to cancel the transmission.
     */
    public Object onPacketOutAsync(Player receiver, Channel channel, Object packet) {
        if (receiver != null) {
            PacketSendEvent event = new PacketSendEvent(receiver, new PacketImpl(packet));
            return event.callIf() ? packet : null;
        } else {
            PacketSendEvent.Handshake event = new PacketSendEvent.Handshake(channel, new PacketImpl(packet));
            return event.callIf() ? packet : null;
        }
    }

    /**
     * Invoked when the server has received a packet from a given player.
     * <p>
     * Use {@link Channel#remoteAddress()} to get the remote address of the client.
     *
     * @param sender  - the player that sent the packet, NULL for early login/status packets.
     * @param channel - channel that received the packet. Never NULL.
     * @param packet  - the packet being received.
     * @return The packet to recieve instead, or NULL to cancel.
     */
    public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
        if (sender != null) {
            PacketReceiveEvent event = new PacketReceiveEvent(sender, new PacketImpl(packet));
            return event.callIf() ? packet : null;
        } else {
            PacketReceiveEvent.Handshake event = new PacketReceiveEvent.Handshake(channel, new PacketImpl(packet));
            return event.callIf() ? packet : null;
        }
    }

    /**
     * Send a packet to a particular player.
     * <p>
     * Note that {@link #onPacketOutAsync(Player, Channel, Object)} will be invoked with this packet.
     *
     * @param player - the destination player.
     * @param packet - the packet to send.
     * @return The future that will be completed when the packet has been sent.
     */
    public ChannelFuture sendPacket(Player player, Object packet) {
        return sendPacket(getChannel(player), packet);
    }

    /**
     * Send a packet to a particular client.
     * <p>
     * Note that {@link #onPacketOutAsync(Player, Channel, Object)} will be invoked with this packet.
     *
     * @param channel - client identified by a channel.
     * @param packet  - the packet to send.
     * @return The future that will be completed when the packet has been sent.
     */
    public ChannelFuture sendPacket(Channel channel, Object packet) {
        return channel.pipeline().writeAndFlush(packet);
    }

    /**
     * Pretend that a given packet has been received from a player.
     * <p>
     * Note that {@link #onPacketInAsync(Player, Channel, Object)} will be invoked with this packet.
     *
     * @param player - the player that sent the packet.
     * @param packet - the packet that will be received by the server.
     */
    public void receivePacket(Player player, Object packet) {
        receivePacket(getChannel(player), packet);
    }

    /**
     * Pretend that a given packet has been received from a given client.
     * <p>
     * Note that {@link #onPacketInAsync(Player, Channel, Object)} will be invoked with this packet.
     *
     * @param channel - client identified by a channel.
     * @param packet  - the packet that will be received by the server.
     */
    public void receivePacket(Channel channel, Object packet) {
        channel.pipeline().context("encoder").fireChannelRead(packet);
    }

    /**
     * Retrieve the name of the channel injector, default implementation is "tiny-" + plugin name + "-" + a unique ID.
     * <p>
     * Note that this method will only be invoked once. It is no longer necessary to override this to support multiple instances.
     *
     * @return A unique channel handler name.
     */
    protected String getHandlerName() {
        return "tiny-" + BukkitPlugin.getInstance().getName() + "-" + ID.incrementAndGet();
    }

    /**
     * Add a custom channel handler to the given player's channel pipeline, allowing us to intercept sent and received packets.
     * <p>
     * This will automatically be called when a player has logged in.
     *
     * @param player - the player to inject.
     */
    public void injectPlayer(Player player) {
        injectChannelInternal(getChannel(player)).player = player;
    }

    /**
     * Add a custom channel handler to the given channel.
     *
     * @param channel - the channel to inject.
     */
    public void injectChannel(Channel channel) {
        injectChannelInternal(channel);
    }

    /**
     * Add a custom channel handler to the given channel.
     *
     * @param channel - the channel to inject.
     * @return The packet interceptor.
     */
    private PacketInterceptor injectChannelInternal(Channel channel) {
        try {
            PacketInterceptor interceptor = (PacketInterceptor) channel.pipeline().get(handlerName);
            // Inject our packet interceptor
            if (interceptor == null) {
                interceptor = new PacketInterceptor();
                channel.pipeline().addBefore("packet_handler", handlerName, interceptor);
                uninjectedChannels.remove(channel);
            }
            return interceptor;
        } catch (IllegalArgumentException e) {
            // Try again
            return (PacketInterceptor) channel.pipeline().get(handlerName);
        }
    }

    /**
     * Retrieve the Netty channel associated with a player. This is cached.
     *
     * @param player - the player.
     * @return The Netty channel.
     */
    public Channel getChannel(Player player) {
        Channel channel = channelLookup.get(player.getName());
        // Lookup channel again
        if (channel == null) {
            Object connection = GET_CONNECTION.get(GET_PLAYER_HANDLE.invoke(player));
            Object manager = GET_MANAGER.get(connection);
            channelLookup.put(player.getName(), channel = GET_CHANNEL.get(manager));
        }
        return channel;
    }

    /**
     * Uninject a specific player.
     *
     * @param player - the injected player.
     */
    public void uninjectPlayer(Player player) {
        uninjectChannel(getChannel(player));
    }

    /**
     * Uninject a specific channel.
     * <p>
     * This will also disable the automatic channel injection that occurs when a player has properly logged in.
     *
     * @param channel - the injected channel.
     */
    public void uninjectChannel(final Channel channel) {
        // No need to guard against this if we're closing
        if (!closed) {
            uninjectedChannels.add(channel);
        }
        // See ChannelInjector in ProtocolLib, line 590
        channel.eventLoop().execute(() -> channel.pipeline().remove(handlerName));
    }

    /**
     * Determine if the given player has been injected by TinyProtocol.
     *
     * @param player - the player.
     * @return TRUE if it is, FALSE otherwise.
     */
    public boolean hasInjected(Player player) {
        return hasInjected(getChannel(player));
    }

    /**
     * Determine if the given channel has been injected by TinyProtocol.
     *
     * @param channel - the channel.
     * @return TRUE if it is, FALSE otherwise.
     */
    public boolean hasInjected(Channel channel) {
        return channel.pipeline().get(handlerName) != null;
    }

    /**
     * Cease listening for packets. This is called automatically when your plugin is disabled.
     */
    public final void close() {
        if (!closed) {
            closed = true;
            // Remove our handlers
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                uninjectPlayer(player);
            }
            // Clean up Bukkit
            HandlerList.unregisterAll(listener);
            unregisterChannelHandler();
        }
    }

    /**
     * Channel handler that is inserted into the player's channel pipeline, allowing us to intercept sent and received packets.
     *
     * @author Kristian
     */
    private final class PacketInterceptor extends ChannelDuplexHandler {

        // Updated by the login event
        public volatile Player player;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // Intercept channel
            final Channel channel = ctx.channel();
            handleLoginStart(channel, msg);
            try {
                msg = onPacketInAsync(player, channel, msg);
            } catch (Exception e) {
                IOKt.severe("Error in onPacketInAsync()");
                e.printStackTrace();
            }
            if (msg != null) {
                super.channelRead(ctx, msg);
            }
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            try {
                msg = onPacketOutAsync(player, ctx.channel(), msg);
            } catch (Exception e) {
                IOKt.severe("Error in onPacketOutAsync()");
                e.printStackTrace();
            }
            if (msg != null) {
                super.write(ctx, msg, promise);
            }
        }

        private void handleLoginStart(Channel channel, Object packet) {
            if (PACKET_LOGIN_IN_START.isInstance(packet)) {
                if (getName != null) {
                    channelLookup.put(getName.get(packet), channel);
                } else {
                    GameProfile profile = getGameProfile.get(packet);
                    channelLookup.put(profile.getName(), channel);
                }
            }
        }
    }
}

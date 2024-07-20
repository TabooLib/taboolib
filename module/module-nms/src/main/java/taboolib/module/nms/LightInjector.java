// MIT License
//
// Copyright (c) 2022 fren_gor
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package taboolib.module.nms;

import com.mojang.authlib.GameProfile;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import taboolib.common.PrimitiveIO;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * A light yet complete and fast packet injector for Spigot servers.
 * <p>
 * Can listen to every packet since {@link AsyncPlayerPreLoginEvent} fires (approximately since the set compression
 * packet, see <a href="https://wiki.vg/Protocol_FAQ#What.27s_the_normal_login_sequence_for_a_client.3F">What's the normal login sequence for a client?</a>).
 * <br>
 * Very rarely, it may happen that some additional login packets get listened before that event is called. However, this
 * shouldn't be an issue for most applications, since login packets aren't usually intercepted anyway.
 * <p>
 * Do not (currently) listen to packets exchanged during status pings (i.e. server list pings).
 * Use the {@link ServerListPingEvent} to change the ping information.
 *
 * @author fren_gor
 */
public abstract class LightInjector {

    private static final Class<?> SERVER_CLASS = getNMSClass("MinecraftServer", "server");
    private static final Class<?> SERVER_CONNECTION_CLASS = getNMSClass("ServerConnection", "server.network");
    private static final Class<?> NETWORK_MANAGER_CLASS = getNMSClass("NetworkManager", "network");
    private static final Class<?> ENTITY_PLAYER_CLASS = getNMSClass("EntityPlayer", "server.level");
    private static final Class<?> PLAYER_CONNECTION_CLASS = getNMSClass("PlayerConnection", "server.network");
    private static final Class<?> PACKET_LOGIN_OUT_SUCCESS_CLASS = getNMSClass("PacketLoginOutSuccess", "network.protocol.login");

    private static final Field NMS_SERVER = getField(getCBClass("CraftServer"), SERVER_CLASS, 1);
    private static final Field NMS_SERVER_CONNECTION = getField(SERVER_CLASS, SERVER_CONNECTION_CLASS, 1);
    private static final Field NMS_NETWORK_MANAGERS_LIST = getField(SERVER_CONNECTION_CLASS, List.class, 2);

    // This field is present only on Paper servers
    // 该字段仅存在于 Paper 服务器上
    @Nullable
    private static final Field NMS_PENDING_NETWORK_MANAGERS = getPendingNetworkManagersFieldOrNull(SERVER_CONNECTION_CLASS);

    private static final Field NMS_CHANNEL_FROM_NM = getField(NETWORK_MANAGER_CLASS, Channel.class, 1);
    private static final Field GAME_PROFILE_FROM_PACKET = getField(PACKET_LOGIN_OUT_SUCCESS_CLASS, GameProfile.class, 1);
    private static final Field GET_PLAYER_CONNECTION = getField(ENTITY_PLAYER_CLASS, PLAYER_CONNECTION_CLASS, 1);
    private static final Field GET_NETWORK_MANAGER = getField(PLAYER_CONNECTION_CLASS, NETWORK_MANAGER_CLASS, 1, 1);

    private static final Method GET_PLAYER_HANDLE = getMethod(getCBClass("entity.CraftPlayer"), "getHandle");

    // Used to make identifiers unique if multiple instances are created. This doesn't need to be atomic
    // since it is called only from the constructor, which is assured to run on the main thread
    // 如果创建了多个实例，用于使标识符唯一。由于它仅在构造函数中调用，并确保在主线程上运行，因此不需要是原子的
    private static int ID = 0;

    private final Plugin plugin;

    // The identifier used to register the ChannelHandler into the channel pipeline
    // 用于将 ChannelHandler 注册到通道管道中的标识符
    private final String identifier;

    // The list of NetworkManagers
    // NetworkManager 的列表
    private final List<?> networkManagers;

    // On Paper there is also a queue/list (depending on the version) of pending NetworkManagers.
    // The list is synchronized using Collections.synchronizedList and is present from 1.9 to 1.14.
    // The queue is an instance of ConcurrentLinkedQueue and replaces the list since 1.15.
    // Both implements Iterable<NetworkManager>
    // 在 Paper 上，还有一个挂起的 NetworkManager 的队列/列表（取决于版本）。
    // 该列表使用 Collections.synchronizedList 进行同步，并且存在于 1.9 到 1.14 版本中。
    // 自 1.15 起，队列是 ConcurrentLinkedQueue 的一个实例，并取代了该列表。
    // 两者都实现了 Iterable<NetworkManager>
    @Nullable
    private final Iterable<?> pendingNetworkManagers;

    private final EventListener listener = new EventListener();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // Used to allow PacketHandlers to set the PacketHandler.player field
    // 用于允许 PacketHandlers 设置 PacketHandler.player 字段
    private final Map<UUID, Player> playerCache = Collections.synchronizedMap(new HashMap<>());
    // Set of already injected channels, used to speed up channel injection during AsyncPlayerPreLoginEvent
    // 已注入通道的集合，用于在 AsyncPlayerPreLoginEvent 期间加速通道注入
    private final Set<Channel> injectedChannels = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    /**
     * Initializes the injector and starts to listen to packets.
     * <p>
     * Note that, while it is possible to create more than one instance per plugin,
     * it's more efficient and recommended to just have only one.
     * <p>
     * 初始化注入器并开始监听数据包。
     * <p>
     * 请注意，虽然每个插件可以创建多个实例，但更高效且推荐的方法是只创建一个。
     *
     * @param plugin The {@link Plugin} which is instantiating this injector.
     *               实例化此注入器的 {@link Plugin}。
     * @throws NullPointerException     If the provided {@code plugin} is {@code null}.
     *                                  如果提供的 {@code plugin} 为 {@code null}。
     * @throws IllegalStateException    When <b>not</b> called from the main thread.
     *                                  当<b>不</b>在主线程中调用时。
     * @throws IllegalArgumentException If the provided {@code plugin} is not enabled.
     *                                  如果提供的 {@code plugin} 未启用。
     */
    public LightInjector(@NotNull Plugin plugin) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("LightInjector must be constructed on the main thread.");
        }
        if (!Objects.requireNonNull(plugin, "Plugin is null.").isEnabled()) {
            throw new IllegalArgumentException("Plugin " + plugin.getName() + " is not enabled");
        }

        this.plugin = plugin;
        this.identifier = Objects.requireNonNull(getIdentifier(), "getIdentifier() returned a null value.") + '-' + ID++;

        try {
            Object conn = NMS_SERVER_CONNECTION.get(NMS_SERVER.get(Bukkit.getServer()));

            if (conn == null) {
                throw new RuntimeException("[LightInjector] ServerConnection is null."); // Should never happen
            }

            networkManagers = (List<?>) NMS_NETWORK_MANAGERS_LIST.get(conn);

            if (NMS_PENDING_NETWORK_MANAGERS != null) {
                pendingNetworkManagers = (Iterable<?>) NMS_PENDING_NETWORK_MANAGERS.get(conn);
            } else {
                pendingNetworkManagers = null;
            }
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("[LightInjector] An error occurred while injecting.", exception);
        }

        Bukkit.getPluginManager().registerEvents(listener, plugin);

        // Inject already online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                injectPlayer(p);
            } catch (Exception exception) {
                plugin.getLogger().log(Level.SEVERE, "[LightInjector] An error occurred while injecting a player:", exception);
            }
        }
    }

    /**
     * Called asynchronously (i.e. not from main thread) when a packet is received from a {@link Player}.
     * 当从 {@link Player} 接收到数据包时异步调用（即不在主线程中）。
     *
     * @param sender  The {@link Player} which sent the packet. May be {@code null} for early login packets.
     *                发送数据包的 {@link Player}。对于早期登录数据包，可能为 {@code null}。
     * @param channel The {@link Channel} of the player's connection.
     *                玩家连接的 {@link Channel}。
     * @param packet  The packet received from player.
     *                从玩家接收到的数据包。
     * @return The packet to receive instead, or {@code null} if the packet should be cancelled.
     * 要接收的替代数据包，或者如果应取消该数据包，则返回 {@code null}。
     */
    protected abstract @Nullable Object onPacketReceiveAsync(@Nullable Player sender, @NotNull Channel channel, @NotNull Object packet);

    /**
     * Called asynchronously (i.e. not from main thread) when a packet is sent to a {@link Player}.
     * 当一个数据包被发送到 {@link Player} 时异步调用（即不在主线程中）。
     *
     * @param receiver The {@link Player} which will receive the packet. May be {@code null} for early login packets.
     *                 将接收数据包的 {@link Player}。对于早期登录数据包，可能为 {@code null}。
     * @param channel  The {@link Channel} of the player's connection.
     *                 玩家连接的 {@link Channel}。
     * @param packet   The packet to send to the player.
     *                 要发送给玩家的数据包。
     * @return The packet to send instead, or {@code null} if the packet should be cancelled.
     * 要发送的替代数据包，或者如果应取消该数据包，则返回 {@code null}。
     */
    protected abstract @Nullable Object onPacketSendAsync(@Nullable Player receiver, @NotNull Channel channel, @NotNull Object packet);

    /**
     * Sends a packet to a player. Since the packet will be sent without any special treatment, this will invoke
     * {@link #onPacketSendAsync(Player, Channel, Object) onPacketSendAsync} when the packet will be intercepted by the
     * injector (any other packet injectors present on the server will intercept and possibly cancel the packet as well).
     * <p>
     * 向玩家发送数据包。由于数据包将被直接发送，因此当注入器拦截到数据包时，将调用 {@link #onPacketSendAsync(Player, Channel, Object) onPacketSendAsync}
     * （服务器上存在的任何其他数据包注入器也将拦截并可能取消数据包）。
     *
     * @param receiver The {@link Player} to which the packet will be sent.
     *                 将接收数据包的 {@link Player}。
     * @param packet   The packet to send.
     *                 要发送的数据包。
     * @throws NullPointerException When a parameter is {@code null}.
     *                              当参数为 {@code null} 时抛出。
     */
    public final void sendPacket(@NotNull Player receiver, @NotNull Object packet) {
        Objects.requireNonNull(receiver, "Player is null.");
        Objects.requireNonNull(packet, "Packet is null.");
        sendPacket(getChannel(receiver), packet);
    }

    /**
     * Sends a packet over a {@link Channel}. Since the packet will be sent without any special treatment, this will invoke
     * {@link #onPacketSendAsync(Player, Channel, Object) onPacketSendAsync} when the packet will be intercepted by the
     * injector (any other packet injectors present on the server will intercept and possibly cancel the packet as well).
     * <p>
     * 通过 {@link Channel} 发送数据包。由于数据包将被直接发送，因此当注入器拦截到数据包时，将调用
     * {@link #onPacketSendAsync(Player, Channel, Object) onPacketSendAsync}（服务器上存在的任何其他数据包注入器也将拦截并可能取消数据包）。
     *
     * @param channel The {@link Channel} on which the packet will be sent.
     *                将发送数据包的 {@link Channel}。
     * @param packet  The packet to send.
     *                要发送的数据包。
     * @throws NullPointerException When a parameter is {@code null}.
     *                              当参数为 {@code null} 时抛出。
     */
    public final void sendPacket(@NotNull Channel channel, @NotNull Object packet) {
        Objects.requireNonNull(channel, "Channel is null.");
        Objects.requireNonNull(packet, "Packet is null.");
        channel.pipeline().writeAndFlush(packet);
    }

    /**
     * Acts like if the server has received a packet from a player. Since this process is done without any special
     * treatment of the packet, this will invoke {@link #onPacketReceiveAsync(Player, Channel, Object) onPacketReceiveAsync}
     * when the packet will be intercepted by the injector (any other packet injectors present on the server will intercept
     * and possibly cancel the packet as well).
     * <p>
     * 模拟服务器从玩家接收到数据包。由于此过程不对数据包进行任何特殊处理，因此当注入器拦截到数据包时，将调用
     * {@link #onPacketReceiveAsync(Player, Channel, Object) onPacketReceiveAsync}（服务器上存在的任何其他数据包注入器也将拦截并可能取消数据包）。
     *
     * @param sender The {@link Player} from which the packet will be received.
     *               将接收数据包的 {@link Player}。
     * @param packet The packet to receive.
     *               要接收的数据包。
     * @throws NullPointerException When a parameter is {@code null}.
     *                              当参数为 {@code null} 时抛出。
     */
    public final void receivePacket(@NotNull Player sender, @NotNull Object packet) {
        Objects.requireNonNull(sender, "Player is null.");
        Objects.requireNonNull(packet, "Packet is null.");
        receivePacket(getChannel(sender), packet);
    }

    /**
     * Acts like if the server has received a packet over a {@link Channel}. Since this process is done without any
     * special treatment of the packet, this will invoke {@link #onPacketReceiveAsync(Player, Channel, Object) onPacketReceiveAsync}
     * when the packet will be intercepted by the injector (any other packet injectors present on the server will intercept
     * and possibly cancel the packet as well).
     * <p>
     * 模拟服务器通过 {@link Channel} 接收到数据包。由于此过程不对数据包进行任何特殊处理，因此当注入器拦截到数据包时，将调用
     * {@link #onPacketReceiveAsync(Player, Channel, Object) onPacketReceiveAsync}（服务器上存在的任何其他数据包注入器也将拦截并可能取消数据包）。
     *
     * @param channel The {@link Channel} on which the packet will be received.
     *                将接收数据包的 {@link Channel}。
     * @param packet  The packet to receive.
     *                要接收的数据包。
     * @throws NullPointerException When a parameter is {@code null} or the provided channel is not a player's channel.
     *                              当参数为 {@code null} 或提供的通道不是玩家的通道时抛出。
     */
    public final void receivePacket(@NotNull Channel channel, @NotNull Object packet) {
        Objects.requireNonNull(channel, "Channel is null.");
        Objects.requireNonNull(packet, "Packet is null.");
        // Fire channel read after encoder in order to run (possible) other injectors code
        // 在编码器之后触发通道读取，以运行（可能的）其他注入器代码
        ChannelHandlerContext encoder = channel.pipeline().context("encoder");
        Objects.requireNonNull(encoder, "Channel is not a player channel").fireChannelRead(packet);
    }

    /**
     * Gets the unique non-null identifier of this injector. A slightly modified version of the returned identifier will
     * be used to register the {@link ChannelHandler} during injection.
     * <p>
     * 获取此注入器的唯一非空标识符。返回的标识符的稍微修改版本将用于在注入期间注册 {@link ChannelHandler}。
     * <p>
     * This method is only called once per instance and should always return the same {@link String} when two instances
     * are constructed by the same {@link Plugin}.
     * <p>
     * 此方法每个实例只调用一次，并且当两个实例由同一个 {@link Plugin} 构造时，应始终返回相同的 {@link String}。
     * <p>
     * The default implementation returns {@code "light-injector-" + getPlugin().getName()}.
     * 默认实现返回 {@code "light-injector-" + getPlugin().getName()}。
     *
     * @return The unique non-null identifier of this injector.
     * 此注入器的唯一非空标识符。
     */
    protected @NotNull String getIdentifier() {
        return "light-injector-" + plugin.getName();
    }

    /**
     * Closes the injector and uninject every injected player. This method is automatically called when the plugin which
     * instantiated this injector disables, so it is usually unnecessary to invoke it directly.
     * <p>
     * 关闭注入器并取消注入每个已注入的玩家。当实例化此注入器的插件禁用时，此方法会自动调用，因此通常不需要直接调用它。
     * <p>
     * The uninjection may require some time, so {@link #onPacketReceiveAsync(Player, Channel, Object) onPacketReceiveAsync}
     * and {@link #onPacketSendAsync(Player, Channel, Object) onPacketSendAsync} might still be called after this method returns.
     * <p>
     * 取消注入可能需要一些时间，因此在此方法返回后，{@link #onPacketReceiveAsync(Player, Channel, Object) onPacketReceiveAsync}
     * 和 {@link #onPacketSendAsync(Player, Channel, Object) onPacketSendAsync} 可能仍会被调用。
     * <p>
     * If this injector is already closed then invoking this method has no effect.
     * 如果此注入器已经关闭，则调用此方法无效。
     */
    public final void close() {
        if (closed.getAndSet(true)) {
            return;
        }

        listener.unregister();

        // Lock out Minecraft
        synchronized (networkManagers) {
            for (Object manager : networkManagers) {
                try {
                    Channel channel = getChannel(manager);

                    // Run on event loop to avoid a possible data race with injection in injectPlayer()
                    // 在事件循环中运行，以避免在 injectPlayer() 中注入时可能发生的数据竞争
                    channel.eventLoop().submit(() -> channel.pipeline().remove(identifier));
                } catch (Exception exception) {
                    plugin.getLogger().log(Level.SEVERE, "[LightInjector] An error occurred while uninjecting a player:", exception);
                }
            }
        }

        playerCache.clear();
        injectedChannels.clear();
    }

    /**
     * Returns whether this injector has been closed.
     * 返回此注入器是否已关闭。
     *
     * @return Whether this injector has been closed.
     * 注入器是否已关闭。
     * @see #close()
     */
    public final boolean isClosed() {
        return closed.get();
    }

    /**
     * Return the plugin which instantiated this injector.
     * 返回实例化此注入器的插件。
     *
     * @return The plugin which instantiated this injector.
     * 实例化此注入器的插件。
     * @see #LightInjector(Plugin)
     */
    public final @NotNull Plugin getPlugin() {
        return plugin;
    }

    private void injectPlayer(Player player) {
        injectChannel(getChannel(player)).player = player;
    }

    private PacketHandler injectChannel(Channel channel) {
        PacketHandler handler = new PacketHandler();

        // Run on event loop to avoid a possible data race with uninjection in close()
        // 在事件循环中运行，以避免在 close() 中取消注入时可能发生的数据竞争
        channel.eventLoop().submit(() -> {
            // Don't inject if uninjection has already occurred in close()
            // 如果在 close() 中已经取消注入，则不进行注入
            if (isClosed()) return;
            // Only inject if not already injected
            // 仅在尚未注入时才进行注入
            if (injectedChannels.add(channel)) {
                try {
                    channel.pipeline().addBefore("packet_handler", identifier, handler);
                } catch (IllegalArgumentException ignored) {
                    plugin.getLogger().severe("[LightInjector] Couldn't inject a player, a handler with identifier '" + identifier + "' is already present");
                }
            }
        });

        return handler;
    }

    private void injectNetworkManager(Object networkManager) {
        Channel channel = getChannel(networkManager);
        // This check avoids useless injections
        // 此检查避免不必要的注入
        if (!injectedChannels.contains(channel)) {
            injectChannel(channel);
        }
    }

    private Object getNetworkManager(Player player) {
        try {
            return GET_NETWORK_MANAGER.get(GET_PLAYER_CONNECTION.get(GET_PLAYER_HANDLE.invoke(player)));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("[LightInjector] Couldn't get player's network manager.", e);
        }
    }

    private Channel getChannel(Player player) {
        return getChannel(getNetworkManager(player));
    }

    private Channel getChannel(Object networkManager) {
        try {
            return (Channel) NMS_CHANNEL_FROM_NM.get(networkManager);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("[LightInjector] Couldn't get network manager's channel.", e);
        }
    }

    // Don't implement Listener for LightInjector in order to hide the event listener from the public API interface
    // 不要为 LightInjector 实现 Listener，以便将事件监听器隐藏在公共 API 接口之外
    private final class EventListener implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        private void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
            PrimitiveIO.dev("[LightInjector] AsyncPlayerPreLoginEvent: %s", event.getName());
            if (isClosed()) {
                return;
            }

            // Inject all not injected managers. Since the AsyncPlayerPreLoginEvent doesn't expose the player's port number,
            // it is impossible to uniquely identify the NetworkManager of the logged player (until forthcoming events
            // are called of course). This is relevant when two players join from the same IP.
            // 注入所有未注入的管理器。由于 AsyncPlayerPreLoginEvent 不公开玩家的端口号，
            // 因此不可能唯一地识别已登录玩家的 NetworkManager（当然，直到后续事件被调用）。
            // 当两个玩家从相同的 IP 加入时，这一点尤为重要。

            // Another solution is to register a permanent object inside server's ServerSocketChannel, however that is
            // not acceptable since LightInjector is meant to be light and reload-safe as much as possible.
            // Thus, this O(n) operation is used to avoid registering any permanent object.
            // 另一种解决方案是在服务器的 ServerSocketChannel 内注册一个永久对象，但这是不可接受的， <-- 指的是 TinyProtocol 的操作方式
            // 因为 LightInjector 旨在尽可能轻量和重载安全。
            // 因此，使用此 O(n) 操作来避免注册任何永久对象。

            // sky:
            // 感觉这里的处理方式就像是早期的 TabooLib Packet Listener，通过逻辑去「猜」玩家的 Channel，只能说这个事件太次了。
            synchronized (networkManagers) { // Lock out main thread
                // 锁定主线程
                if (networkManagers instanceof RandomAccess) {
                    // Faster for loop
                    // 更快的 for 循环
                    // Iterating backwards is better since new NetworkManagers should be added at the end of the list
                    // 反向迭代更好，因为新的 NetworkManager 应该添加到列表的末尾
                    for (int i = networkManagers.size() - 1; i >= 0; i--) {
                        Object networkManager = networkManagers.get(i);
                        injectNetworkManager(networkManager);
                        PrimitiveIO.dev("[LightInjector] injectNetworkManager in RandomAccess");
                    }
                } else {
                    // Using standard foreach to avoid any potential performance issues
                    // (networkManagers should be an ArrayList, but we cannot be sure about that due to forks)
                    // 使用标准的 foreach 循环以避免任何潜在的性能问题
                    //（networkManagers 应该是一个 ArrayList，但由于分叉我们不能确定这一点）
                    for (Object networkManager : networkManagers) {
                        injectNetworkManager(networkManager);
                        PrimitiveIO.dev("[LightInjector] injectNetworkManager in else");
                    }
                }

                if (pendingNetworkManagers != null) {
                    // On Paper servers, inject NetworkManagers inside the pending queue/list.
                    // 在 Paper 服务器上，将 NetworkManager 注入到挂起的队列/列表中。

                    // Synchronize here since on 1.9-1.14 pendingNetworkManagers is a synchronized list (see Collections#synchronizedList)
                    // 在此处同步，因为在 1.9-1.14 版本中 pendingNetworkManagers 是一个同步列表（参见 Collections#synchronizedList）
                    synchronized (pendingNetworkManagers) {
                        for (Object networkManager : pendingNetworkManagers) {
                            injectNetworkManager(networkManager);
                            PrimitiveIO.dev("[LightInjector] injectNetworkManager in pendingNetworkManagers");
                        }
                    }
                }
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        private void onPlayerLoginEvent(PlayerLoginEvent event) {
            if (isClosed()) {
                return;
            }
            // Save Player object for later
            playerCache.put(event.getPlayer().getUniqueId(), event.getPlayer());
        }

        @EventHandler(priority = EventPriority.LOWEST)
        private void onPlayerJoinEvent(PlayerJoinEvent event) {
            if (isClosed()) {
                return;
            }
            Player player = event.getPlayer();

            // At worst, if player hasn't successfully been injected in the previous steps, it's injected now.
            // 最坏情况下，如果玩家在之前的步骤中未成功注入，现在将进行注入。
            // At this point the Player's PlayerConnection field should have been initialized to a non-null value
            // 此时，玩家的 PlayerConnection 字段应已初始化为非空值
            Object networkManager = getNetworkManager(player);
            Channel channel = getChannel(networkManager);
            @Nullable ChannelHandler channelHandler = channel.pipeline().get(identifier);
            if (channelHandler != null) {
                // A channel handler named identifier has been found
                // 找到了一个名为 identifier 的通道处理程序
                if (channelHandler instanceof PacketHandler) {
                    // The player has already been injected, only set the player as a backup in the eventuality
                    // that anything else failed to set it previously.
                    // 玩家已被注入，只需将玩家设置为备份，以防其他任何操作未能先前设置。
                    ((PacketHandler) channelHandler).player = player;
                    // Clear the cache to avoid any possible (but very unlikely to happen) memory leak
                    // 清除缓存以避免任何可能（但非常不可能发生）的内存泄漏
                    playerCache.remove(player.getUniqueId());
                }
                // Don't inject again
                // 不要再次注入
                return;
            }
            plugin.getLogger().info("[LightInjector] Late injection for player " + player.getName());
            injectChannel(channel).player = player;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private void onPluginDisableEvent(PluginDisableEvent event) {
            if (plugin.equals(event.getPlugin())) {
                close();
            }
        }

        private void unregister() {
            AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
            PlayerLoginEvent.getHandlerList().unregister(this);
            PlayerJoinEvent.getHandlerList().unregister(this);
            PluginDisableEvent.getHandlerList().unregister(this);
        }
    }

    private final class PacketHandler extends ChannelDuplexHandler {

        private volatile Player player;

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
        public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
            if (player == null && PACKET_LOGIN_OUT_SUCCESS_CLASS.isInstance(packet)) {
                // Player object should be in cache. If it's not, then it'll be PlayerJoinEvent to set the player
                // 玩家对象应该在缓存中。如果不在缓存中，则将在 PlayerJoinEvent 中设置玩家
                try {
                    @Nullable Player player = playerCache.remove(((GameProfile) GAME_PROFILE_FROM_PACKET.get(packet)).getId());
                    // Set the player only if it was contained in the cache
                    // 仅在缓存中包含玩家时才设置玩家
                    if (player != null) {
                        this.player = player;
                    }
                } catch (ReflectiveOperationException exception) {
                    plugin.getLogger().log(Level.SEVERE, "[LightInjector] An error occurred while handling PacketLoginOutSuccess:", exception);
                }
            }
            @Nullable Object newPacket;
            try {
                newPacket = onPacketSendAsync(player, ctx.channel(), packet);
            } catch (OutOfMemoryError error) {
                // Out of memory, re-throw and return immediately
                throw error;
            } catch (Throwable throwable) {
                plugin.getLogger().log(Level.SEVERE, "[LightInjector] An error occurred while calling onPacketSendAsync:", throwable);
                super.write(ctx, packet, promise);
                return;
            }
            if (newPacket != null)
                super.write(ctx, newPacket, promise);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
            @Nullable Object newPacket;
            try {
                newPacket = onPacketReceiveAsync(player, ctx.channel(), packet);
            } catch (OutOfMemoryError error) {
                // Out of memory, re-throw and return immediately
                throw error;
            } catch (Throwable throwable) {
                plugin.getLogger().log(Level.SEVERE, "[LightInjector] An error occurred while calling onPacketReceiveAsync:", throwable);
                super.channelRead(ctx, packet);
                return;
            }
            if (newPacket != null)
                super.channelRead(ctx, newPacket);
        }
    }

    // ====================================== Reflection stuff ======================================

    private static Class<?> getNMSClass(String name, String mcPackage) {
        String clazz;
        // NOTICE 从 1.17+ 开始, NMS 不再带有版本号
        if (MinecraftVersion.INSTANCE.isUniversal()) {
            clazz = "net.minecraft." + mcPackage + '.' + name;
        } else {
            clazz = "net.minecraft.server." + MinecraftVersion.INSTANCE.getMinecraftVersion() + '.' + name;
        }
        try {
            return LightReflection.forName(clazz);
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException("[LightInjector] Cannot find NMS Class! (" + clazz + ')', exception);
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
            return LightReflection.forName(clazz);
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException("[LightInjector] Cannot find CB Class! (" + clazz + ')', exception);
        }
    }

    private static Field getField(Class<?> clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("[LightInjector] Cannot find field! (" + clazz.getName() + '.' + name + ')', exception);
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

        String errorMsg = "[LightInjector] Cannot find field! (" + savedIndex + getOrdinal(savedIndex) + type.getName() + " in " + savedClazz.getName();
        if (superClassesToTry > 0) {
            errorMsg += " and in its " + superClassesToTry + (superClassesToTry == 1 ? " super class" : " super classes");
        }
        errorMsg += ')';

        throw new RuntimeException(errorMsg);
    }

    @Nullable
    private static Field getPendingNetworkManagersFieldOrNull(Class<?> serverConnectionClass) {
        try {
            // The field's name shouldn't be obscured since it's been added by Paper
            Field pending = getField(serverConnectionClass, "pending");
            if (pending.getType() == Queue.class || pending.getType() == List.class) {
                return pending;
            }
        } catch (Exception ignored) {
        }

        // No field named pending, try with the queue first
        try {
            return getField(serverConnectionClass, Queue.class, 1);
        } catch (Exception ignored) {
        }
        try {
            return getField(serverConnectionClass, List.class, 3);
        } catch (Exception ignored) {
        }
        return null;
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
            throw new RuntimeException("[LightInjector] Cannot find method! (" + clazz.getName() + '.' + name + '(' + params.toString() + ')', exception);
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

        throw new RuntimeException("[LightInjector] Cannot find method! (" + savedIndex + getOrdinal(savedIndex) + " returning " + returnType.getName() + " in " + clazz.getName() + ')');
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
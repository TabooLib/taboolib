package me.skymc.taboolib.team;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.base.Preconditions;
import com.ilummc.tlib.bungee.api.ChatColor;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.events.itag.AsyncPlayerReceiveNameTagEvent;
import me.skymc.taboolib.events.itag.PlayerReceiveNameTagEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.stream.IntStream;

/**
 * @Author sky
 * @Since 2018-05-09 21:03
 */
public class TagAPI implements Listener {

    private static final int[] uuidSplit = new int[]{0, 8, 12, 16, 20, 32};

    private static boolean loaded = false;
    private static HashMap<Integer, Player> entityIdMap = new HashMap<>();
    private static HashMap<UUID, String> playerData = new HashMap<>();

    TagAPI() {
    }

    public static void inst() {
        assert !loaded : "TagAPI is already instanced!";
        loaded = true;

        Bukkit.getServer().getOnlinePlayers().forEach(player -> entityIdMap.put(player.getEntityId(), player));
        Bukkit.getPluginManager().registerEvents(new TagAPI(), Main.getInst());
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Main.getInst(), PacketType.Play.Server.PLAYER_INFO) {

            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacket().getPlayerInfoAction().read(0) != EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
                    return;
                }

                List<PlayerInfoData> newPlayerInfo = new ArrayList<>();
                for (PlayerInfoData playerInfo : event.getPacket().getPlayerInfoDataLists().read(0)) {
                    Player player;
                    if (playerInfo == null || playerInfo.getProfile() == null || (player = Bukkit.getServer().getPlayer(playerInfo.getProfile().getUUID())) == null) {
                        // Unknown Player
                        newPlayerInfo.add(playerInfo);
                        continue;
                    }
                    newPlayerInfo.add(new PlayerInfoData(getSentName(player.getEntityId(), playerInfo.getProfile(), event.getPlayer()), playerInfo.getPing(), playerInfo.getGameMode(), playerInfo.getDisplayName()));
                }
                event.getPacket().getPlayerInfoDataLists().write(0, newPlayerInfo);
            }
        });
    }

    public static String getPlayerDisplayName(Player player) {
        return playerData.getOrDefault(player.getUniqueId(), player.getName());
    }

    public static void setPlayerDisplayName(Player player, String name) {
        String nameColored = ChatColor.translateAlternateColorCodes('&', name);
        player.setDisplayName(nameColored);
        player.setPlayerListName(nameColored);
        playerData.put(player.getUniqueId(), nameColored);

        Bukkit.getScheduler().runTask(Main.getInst(), () -> refreshPlayer(player));
    }

    public static void removePlayerDisplayName(Player player) {
        player.setDisplayName(null);
        player.setPlayerListName(null);
        playerData.remove(player.getUniqueId());

        Bukkit.getScheduler().runTask(Main.getInst(), () -> refreshPlayer(player));
    }

    public static void refreshPlayer(Player player) {
        Preconditions.checkState(Main.getInst().isEnabled(), "Not Enabled!");
        Preconditions.checkNotNull(player, "player");

        player.getWorld().getPlayers().forEach(playerFor -> refreshPlayer(player, playerFor));
    }

    public static void refreshPlayer(final Player player, final Player forWhom) {
        Preconditions.checkState(Main.getInst().isEnabled(), "Not Enabled!");
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(forWhom, "forWhom");

        if (player != forWhom && player.getWorld() == forWhom.getWorld() && forWhom.canSee(player)) {
            forWhom.hidePlayer(player);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInst(), () -> forWhom.showPlayer(player), 2);
        }
    }

    public static void refreshPlayer(Player player, Set<Player> forWhom) {
        Preconditions.checkState(Main.getInst().isEnabled(), "Not Enabled!");
        Preconditions.checkNotNull(player, "player");
        Preconditions.checkNotNull(forWhom, "forWhom");

        forWhom.forEach(playerFor -> refreshPlayer(player, playerFor));
    }

    private static WrappedGameProfile getSentName(int sentEntityId, WrappedGameProfile sent, Player destinationPlayer) {
        Preconditions.checkState(Bukkit.getServer().isPrimaryThread(), "Can only process events on main thread.");

        Player namedPlayer = entityIdMap.get(sentEntityId);
        if (namedPlayer == null) {
            // They probably were dead when we reloaded
            return sent;
        }

        PlayerReceiveNameTagEvent oldEvent = new PlayerReceiveNameTagEvent(destinationPlayer, namedPlayer, sent.getName());
        Bukkit.getServer().getPluginManager().callEvent(oldEvent);

        StringBuilder builtUUID = new StringBuilder();
        if (!sent.getId().contains("-")) {
            IntStream.range(0, uuidSplit.length - 1).forEach(i -> builtUUID.append(sent.getId(), uuidSplit[i], uuidSplit[i + 1]).append("-"));
        } else {
            builtUUID.append(sent.getId());
        }

        AsyncPlayerReceiveNameTagEvent newEvent = new AsyncPlayerReceiveNameTagEvent(destinationPlayer, namedPlayer, getPlayerDisplayName(namedPlayer), UUID.fromString(builtUUID.toString()));
        Bukkit.getServer().getPluginManager().callEvent(newEvent);

        updatePlayerTag(namedPlayer, newEvent);
        return new WrappedGameProfile(newEvent.getUUID(), newEvent.getTag().substring(0, Math.min(newEvent.getTag().length(), 16)));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        entityIdMap.put(event.getPlayer().getEntityId(), event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        entityIdMap.remove(event.getPlayer().getEntityId());
    }

    private static void updatePlayerTag(Player namedPlayer, AsyncPlayerReceiveNameTagEvent newEvent) {
        TagManager.PlayerData playerData = TagManager.getInst().getPlayerData(namedPlayer);
        if (playerData.isEmpty()) {
            return;
        }
        TagManager.getInst().unloadData(namedPlayer);
        playerData.setName(newEvent.getTag());
        TagManager.getInst().uploadData(namedPlayer);
    }
}

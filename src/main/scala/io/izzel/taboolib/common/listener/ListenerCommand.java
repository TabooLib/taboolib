package io.izzel.taboolib.common.listener;

import com.google.common.collect.Lists;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.common.loader.Startup;
import io.izzel.taboolib.common.loader.StartupLoader;
import io.izzel.taboolib.module.ai.SimpleAiSelector;
import io.izzel.taboolib.module.command.lite.CommandBuilder;
import io.izzel.taboolib.module.db.local.Local;
import io.izzel.taboolib.module.db.local.LocalPlayer;
import io.izzel.taboolib.module.hologram.Hologram;
import io.izzel.taboolib.module.hologram.THologram;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.module.light.TLight;
import io.izzel.taboolib.module.lite.SimpleReflection;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.module.nms.impl.Type;
import io.izzel.taboolib.module.tellraw.TellrawJson;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.book.BookFormatter;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.lite.Effects;
import io.izzel.taboolib.util.lite.Signs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author sky
 */
@TListener
public class ListenerCommand implements Listener {

    static {
        StartupLoader.register(ListenerCommand.class);
    }

    abstract static class Module {

        abstract public String[] name();

        abstract public void run(Player player, String[] args);
    }

    List<Module> testUtil = Lists.newArrayList(
            new Module() {
                @Override
                public String[] name() {
                    return new String[] {"json", "tellrawJson"};
                }

                @Override
                public void run(Player player, String[] args) {
                    TellrawJson.create()
                            .append("§8[§fTabooLib§8] §7TellrawJson: §f[")
                            .append(Items.getName(player.getItemInHand())).hoverItem(player.getItemInHand())
                            .append("§f]")
                            .send(player);
                }
            },
            new Module() {
                @Override
                public String[] name() {
                    return new String[] {"sign", "fakeSign"};
                }

                @Override
                public void run(Player player, String[] args) {
                    Signs.fakeSign(player, lines -> player.sendMessage("§8[§fTabooLib§8] §7FakeSign: §f" + Arrays.toString(lines)));
                }
            },
            new Module() {
                @Override
                public String[] name() {
                    return new String[] {"hd", "hologram"};
                }

                @Override
                public void run(Player player, String[] args) {
                    player.sendMessage("§8[§fTabooLib§8] §7Hologram.");
                    Location location = player.getEyeLocation().add(player.getLocation().getDirection());
                    Hologram hologram = THologram.create(location, "TabooLib", player)
                            .flash(Lists.newArrayList(
                                    "§bT§fabooLib",
                                    "§bTa§fbooLib",
                                    "§bTab§fooLib",
                                    "§bTabo§foLib",
                                    "§bTaboo§fLib",
                                    "§bTabooL§fib",
                                    "§bTabooLi§fb",
                                    "§bTabooLib",
                                    "§bTabooLi§fb",
                                    "§bTabooL§fib",
                                    "§bTaboo§fLib",
                                    "§bTabo§foLib",
                                    "§bTab§fooLib",
                                    "§bTa§fbooLib",
                                    "§bT§fabooLib",
                                    "§fTabooLib"
                            ), 1).deleteOn(30);
                }
            },
            new Module() {
                @Override
                public String[] name() {
                    return new String[] {"book", "bookBuilder"};
                }

                @Override
                public void run(Player player, String[] args) {
                    BookFormatter.writtenBook()
                            .generation(BookMeta.Generation.COPY_OF_COPY)
                            .addPage(TellrawJson.create()
                                    .append("BookBuilder")
                                    .hoverText("HoverText"))
                            .open(player);
                }
            },
            new Module() {
                @Override
                public String[] name() {
                    return new String[] {"light"};
                }

                @Override
                public void run(Player player, String[] args) {
                    player.sendMessage("§8[§fTabooLib§8] §7Lighting. §a(+)");
                    TLight.create(player.getLocation().getBlock(), Type.BLOCK, 15);
                    TabooLib.getPlugin().runTask(() -> {
                        TLight.create(player.getLocation().getBlock(), Type.BLOCK, 5);
                        player.sendMessage("§8[§fTabooLib§8] §7Lighting. §c(-)");
                    }, 20);
                    TabooLib.getPlugin().runTask(() -> {
                        TLight.delete(player.getLocation().getBlock(), Type.BLOCK);
                        player.sendMessage("§8[§fTabooLib§8] §7Lighting. §8(-)");
                    }, 40);
                }
            },
            new Module() {
                @Override
                public String[] name() {
                    return new String[] {"simpleAI", "ai"};
                }

                @Override
                public void run(Player player, String[] args) {
                    player.sendMessage("§8[§fTabooLib§8] §7SimpleAI.");
                    Skeleton skeleton = player.getWorld().spawn(player.getLocation(), Skeleton.class, c -> {
                        c.setCustomName("Fearless Skeleton");
                        c.setCustomNameVisible(true);
                    });
                    TabooLib.getPlugin().runTask(() -> {
                        SimpleAiSelector.getExecutor().getGoalAi(skeleton).forEach(ai -> {
                            player.sendMessage("§8[§fTabooLib§8] §7AI (Origin): §8" + SimpleReflection.getFieldValueChecked(ai.getClass(), ai, "a", true));
                        });
                        SimpleAiSelector.getExecutor().removeGoalAi(skeleton, 3);
                        SimpleAiSelector.getExecutor().getGoalAi(skeleton).forEach(ai -> {
                            player.sendMessage("§8[§fTabooLib§8] §7AI (After): §8" + SimpleReflection.getFieldValueChecked(ai.getClass(), ai, "a", true));
                        });
                    }, 20);
                }
            },
            new Module() {
                @Override
                public String[] name() {
                    return new String[] {"local"};
                }

                @Override
                public void run(Player player, String[] args) {
                    TellrawJson.create().append("§8[§fTabooLib§8] §7LocalPlayer: ").append("§c[...]").hoverText(LocalPlayer.get(player).saveToString()).send(player);
                    long time = System.currentTimeMillis();
                    FileConfiguration conf = LocalPlayer.get0(player);
                    player.sendMessage("§8[§fTabooLib§8] §7get: " + (System.currentTimeMillis() - time) + "ms");
                    time = System.currentTimeMillis();
                    LocalPlayer.set0(player, conf);
                    player.sendMessage("§8[§fTabooLib§8] §7set: " + (System.currentTimeMillis() - time) + "ms");
                }
            },
            new Module() {
                @Override
                public String[] name() {
                    return new String[] {"effects", "effect"};
                }

                @Override
                public void run(Player player, String[] args) {
                    if (args.length < 2) {
                        player.sendMessage("§8[§fTabooLib§8] §7Effects:");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f LINE-[interval]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f POLYGON-[radius]-[interval]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f CIRCLE-[radius]-[rate]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f CONE-[height]-[radius]-[rate]-[circle rate]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f ATOM-[orbits]-[radius]-[rate]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f ELLIPSE-[radius]-[other radius]-[rate]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f INFINITY-[radius]-[rate]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f CRESCENT-[radius]-[rate]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f WARE_FUNCTION-[extend]-[height range]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f CYLINDER-[height]-[radius]-[rate]-[interval]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f SPHERE-[radius]-[rate]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f SPHERE_SPIKE-[radius]-[rate]-[chance]-[min]-[max]-[interval]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f RING-[rate]-[tube rate]-[radius]-[tube radius]");
                        TellrawJson.create().append("§8[§fTabooLib§8] §7-§f LIGHTING-").append("§c[...]").hoverText("[rate]-[direction]-[entries]-[branches]-[radius]-[offset]-[offset rate]-[length]-[length rate]-[branch]-[branch rate]").send(player);
                        player.sendMessage("§8[§fTabooLib§8] §7-§f DNA-[radius]-[rate]-[extension]-[height]-[hydrogen bond dist]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f RECTANGLE-[rate]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f CAGE-[rate]-[bar rate]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f CUBE-[rate]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f CUBE_FILLED-[rate]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f CUBE_STRUCTURED-[rate]");
                        player.sendMessage("§8[§fTabooLib§8] §7-§f HYPERCUBE-[rate]-[size rate]-[cubes]");
                        return;
                    }
                    List<String> a = Lists.newArrayList(args[1].toUpperCase().split("-"));
                    Location locA = player.getEyeLocation();
                    Location locB = player.getEyeLocation().add(player.getLocation().getDirection().multiply(10)).add(Vector.getRandom().multiply(5));
                    Consumer<Location> action1 = loc -> Effects.create(Particle.FLAME, loc).count(1).player(player).play();
                    Consumer<Location> action2 = loc -> Effects.create(Particle.VILLAGER_HAPPY, loc).count(1).player(player).play();
                    switch (a.get(0)) {
                        case "LINE": {
                            Effects.buildLine(locA, locB, action1, orDob(a, 1, 0.1));
                            break;
                        }
                        case "POLYGON": {
                            Effects.buildPolygon(locA, orDob(a, 1, 10D), orDob(a, 2, 10D), action1);
                            break;
                        }
                        case "CIRCLE": {
                            Effects.buildCircle(locA, orDob(a, 1, 10D), orDob(a, 2, 10D), action1);
                            break;
                        }
                        case "CONE": {
                            Effects.buildCone(locA, orDob(a, 1, 10D), orDob(a, 2, 10D), orDob(a, 3, 10D), orDob(a, 4, 10D), action1);
                            break;
                        }
                        case "ATOM": {
                            Effects.buildAtom(locA, orInt(a, 1, 10), orDob(a, 2, 10D), orDob(a, 3, 10D), action1, action2);
                            break;
                        }
                        case "ELLIPSE": {
                            Effects.buildEllipse(locA, orDob(a, 1, 10D), orDob(a, 2, 10D), orDob(a, 3, 10D), action1);
                            break;
                        }
                        case "INFINITY": {
                            Effects.buildInfinity(locA, orDob(a, 1, 10D), orDob(a, 2, 10D), action1);
                            break;
                        }
                        case "CRESCENT": {
                            Effects.buildCrescent(locA, orDob(a, 1, 10D), orDob(a, 2, 10D), action1);
                            break;
                        }
                        case "WARE_FUNCTION": {
                            Effects.buildWaveFunction(locA, orDob(a, 1, 10D), orDob(a, 2, 10D), orDob(a, 3, 10D), orDob(a, 4, 10D), action1);
                            break;
                        }
                        case "CYLINDER": {
                            Effects.buildCylinder(locA, orDob(a, 1, 10D), orDob(a, 2, 10D), orDob(a, 3, 10D), orDob(a, 4, 10D), action1);
                            break;
                        }
                        case "SPHERE": {
                            Effects.buildSphere(locA, orDob(a, 1, 10D), orDob(a, 2, 10D), action1);
                            break;
                        }
                        case "SPHERE_SPIKE": {
                            Effects.buildSphereSpike(locA, orDob(a, 1, 10D), orDob(a, 2, 10D), orInt(a, 3, 10), orDob(a, 4, 10D), orDob(a, 5, 10D), orDob(a, 6, 10D), action1);
                            break;
                        }
                        case "RING": {
                            Effects.buildRing(locA, orDob(a, 1, 10D), orDob(a, 2, 10D), orDob(a, 3, 10D), orDob(a, 4, 10D), action1);
                            break;
                        }
                        case "LIGHTING": {
                            Effects.buildLightning(locA, locA.getDirection(), orInt(a, 1, 10), orInt(a, 2, 10), orDob(a, 3, 10), orDob(a, 4, 10D), orDob(a, 5, 10D), orDob(a, 6, 10D), orDob(a, 7, 10D), orDob(a, 8, 10D), orDob(a, 9, 10D), action1);
                            break;
                        }
                        case "DNA": {
                            Effects.buildDNA(locA, orDob(a, 1, 10D), orDob(a, 2, 10D), orDob(a, 3, 10D), orInt(a, 4, 10), orInt(a, 5, 10), action1, action2);
                            break;
                        }
                        case "RECTANGLE": {
                            Effects.buildRectangle(locA, locB, orDob(a, 1, 10D), action1);
                            break;
                        }
                        case "CAGE": {
                            Effects.buildCage(locA, locB, orDob(a, 1, 10D), orDob(a, 2, 10D), action1);
                            break;
                        }
                        case "CUBE": {
                            Effects.buildCube(locA, locB, orDob(a, 1, 10D), action1);
                            break;
                        }
                        case "CUBE_FILLED": {
                            Effects.buildCubeFilled(locA, locB, orDob(a, 1, 10D), action1);
                            break;
                        }
                        case "CUBE_STRUCTURED": {
                            Effects.buildCubeStructured(locA, locB, orDob(a, 1, 10D), action1);
                            break;
                        }
                        case "HYPERCUBE": {
                            Effects.buildHypercube(locA, locB, orDob(a, 1, 10D), orDob(a, 2, 10D), orInt(a, 3, 10), action1);
                            break;
                        }
                        default:
                            player.sendMessage("§8[§fTabooLib§8] §7No Effect.");
                            break;
                    }
                }
            });


    @Startup.Starting
    public void init() {
        // 版本命令
        CommandBuilder.create("taboolib", TabooLib.getPlugin())
                .aliases("lib")
                .execute((sender, args) -> {
                    sender.sendMessage("§8[§fTabooLib§8] §7Currently Version: §fv" + TabooLib.getVersion());
                }).build();
        // 调试命令
        CommandBuilder.create("taboolibtest", TabooLib.getPlugin())
                .permission("*")
                .aliases("libtest")
                .tab((sender, args) -> testUtil.stream().flatMap(module -> Arrays.stream(module.name())).filter(name -> name.toLowerCase().startsWith(args[0])).collect(Collectors.toList()))
                .execute((sender, args) -> {
                    if (sender instanceof Player) {
                        if (args.length == 0) {
                            sender.sendMessage("§8[§fTabooLib§8] §7/libtest §8[...]");
                            return;
                        }
                        for (Module module : testUtil) {
                            for (String name : module.name()) {
                                if (name.equalsIgnoreCase(args[0])) {
                                    module.run((Player) sender, args);
                                    return;
                                }
                            }
                        }
                        sender.sendMessage("§8[§fTabooLib§8] §7Test: §f" + testUtil.stream().map(i -> i.name()[0]).collect(Collectors.joining(", ")));
                    }
                }).build();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void cmd(ServerCommandEvent e) {
        if (e.getCommand().equalsIgnoreCase("saveFiles")) {
            Local.saveFiles();
            LocalPlayer.saveFiles();
            TLogger.getGlobalLogger().info("Successfully.");
        } else if (e.getCommand().equalsIgnoreCase("tDebug")) {
            if (TabooLibAPI.isDebug()) {
                TabooLibAPI.debug(false);
                TLogger.getGlobalLogger().info("&cDisabled.");
            } else {
                TabooLibAPI.debug(true);
                TLogger.getGlobalLogger().info("&aEnabled.");
            }
        } else if (e.getCommand().equalsIgnoreCase("libUpdate")) {
            e.setCancelled(true);
            e.getSender().sendMessage("§8[§fTabooLib§8] §cWARNING §7| §4Update TabooLib will force to restart your server. Please confirm this action by type §c/libupdateconfirm");
        } else if (e.getCommand().equalsIgnoreCase("libUpdateConfirm") || e.getCommand().equalsIgnoreCase("libUpdate confirm")) {
            e.getSender().sendMessage("§8[§fTabooLib§8] §7Downloading TabooLib file...");
            Files.downloadFile("https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/TabooLib.jar", new File("libs/TabooLib.jar"));
            e.getSender().sendMessage("§8[§fTabooLib§8] §2Download completed, the server will restart in 3 secs");
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            Bukkit.shutdown();
        }
    }

    private static int orInt(List<String> list, int index, int def) {
        return list.size() > index ? NumberConversions.toInt(list.get(index)) : def;
    }

    private static double orDob(List<String> list, int index, double def) {
        return list.size() > index ? NumberConversions.toDouble(list.get(index)) : def;
    }
}

package io.izzel.taboolib.util;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.Lists;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.inject.PlayerContainer;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.util.book.builder.BookBuilder;
import io.izzel.taboolib.util.chat.TextComponent;
import io.izzel.taboolib.util.item.ItemBuilder;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.lite.Catchers;
import io.izzel.taboolib.util.lite.Numbers;
import io.izzel.taboolib.util.lite.Scoreboards;
import io.izzel.taboolib.util.lite.Signs;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * 简化一些逻辑，将之前找不到的工具合并到一起
 *
 * @author sky
 * @since 2020-10-02 03:32
 */
public class Features {

    private static ScriptEngine scriptEngine;

    @PlayerContainer
    static final ConcurrentHashMap<String, Consumer<List<String>>> inputBookMap = new ConcurrentHashMap<>();

    @TListener
    static class FeaturesListener implements Listener {

        @EventHandler
        public void e(PlayerEditBookEvent e) {
            List<String> bookLore = e.getNewBookMeta().getLore();
            if (bookLore != null && bookLore.size() > 0 && bookLore.get(0).equals("§0Features Input")) {
                Consumer<List<String>> consumer = inputBookMap.get(e.getPlayer().getName());
                if (consumer != null) {
                    List<String> pages = Lists.newArrayList();
                    for (String page : e.getNewBookMeta().getPages()) {
                        pages.addAll(Arrays.asList(new TextComponent(page).toPlainText().replace("§0", "").split("\n")));
                    }
                    consumer.accept(pages);
                    // 一次性捕获
                    if (bookLore.size() > 1 && bookLore.get(1).equals("§0Disposable")) {
                        inputBookMap.remove(e.getPlayer().getName());
                        Items.takeItem(e.getPlayer().getInventory(), i -> Items.hasLore(i, "Features Input"), 99);
                    }
                }
            }
        }
    }

    static {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        try {
            NashornScriptEngineFactory factory = (NashornScriptEngineFactory) scriptEngineManager.getEngineFactories().stream().filter(factories -> "Oracle Nashorn".equalsIgnoreCase(factories.getEngineName())).findFirst().orElse(null);
            scriptEngine = Objects.requireNonNull(factory).getScriptEngine("-doe", "--global-per-engine");
        } catch (Exception ignored) {
            scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
        }
    }

    /**
     * 向玩家播放记分板
     * 待重置，暂用 Scoreboards
     *
     * @param player 玩家
     * @param lines  记分板内容
     */
    @NotNull
    public static Scoreboard displayScoreboard(Player player, String... lines) {
        return Scoreboards.display(player, lines);
    }

    /**
     * 预编译脚本
     *
     * @param script 脚本源码
     * @return 预编译脚本实例
     */
    @Nullable
    public static CompiledScript compileScript(String script) {
        try {
            return ((Compilable) scriptEngine).compile(script);
        } catch (Exception e) {
            TLogger.getGlobalLogger().info("§4JavaScript §c" + script + "§4 Compile Failed: §c" + e.toString());
            return null;
        }
    }

    /**
     * 向玩家发送虚拟牌子，并捕获玩家接下来的编辑内容
     * 待重置，暂用 Signs
     *
     * @param player  玩家
     * @param catcher 编辑内容
     */
    public static void inputSign(Player player, Consumer<String[]> catcher) {
        Signs.fakeSign(player, new String[0], catcher);
    }

    /**
     * 向玩家发送虚拟牌子，并捕获玩家接下来的编辑内容
     * 待重置，暂用 Signs
     *
     * @param player  玩家
     * @param origin  原始内容
     * @param catcher 编辑内容
     */
    public static void inputSign(Player player, String[] origin, Consumer<String[]> catcher) {
        Signs.fakeSign(player, origin, catcher);
    }

    /**
     * 捕获玩家接下来的聊天内容，并结束之前的任务
     * 待重置，暂用 Catchers
     *
     * @param player 玩家
     * @param input  聊天内容捕获实例
     */
    public static void inputChat(Player player, ChatInput input) {
        List<Catchers.Catcher> catchers = Catchers.getPlayerdata().remove(player.getName());
        if (catchers != null) {
            for (Catchers.Catcher catcher : catchers) {
                catcher.cancel();
            }
        }
        Catchers.call(player, new Catchers.Catcher() {

            @Override
            public String quit() {
                return input.quit();
            }

            @Override
            public Catchers.Catcher before() {
                input.head();
                return this;
            }

            @Override
            public void cancel() {
                input.cancel();
            }

            @Override
            public boolean after(String s) {
                return input.onChat(s);
            }
        });
    }

    /**
     * 向玩家发送一本书
     * 并捕获该书本的编辑动作
     *
     * @param player     玩家
     * @param display    展示名称
     * @param disposable 编辑后销毁
     * @param origin     原始内容
     * @param catcher    编辑动作
     */
    public static void inputBook(Player player, String display, Boolean disposable, List<String> origin, Consumer<List<String>> catcher) {
        // 移除正在编辑的书本
        Items.takeItem(player.getInventory(), i -> Items.hasLore(i, "Features Input"), 99);
        // 发送书本
        player.getInventory().addItem(
                new ItemBuilder(
                        new BookBuilder(XMaterial.WRITABLE_BOOK.parseItem())
                                .pagesRaw(String.join("\n", origin))
                                .build()
                ).name("§f" + display).lore("§0Features Input", disposable ? "§0Disposable" : "").build()
        );
        inputBookMap.put(player.getName(), catcher);
    }

    /**
     * 从伤害事件中获取攻击实体
     * 在 5.47 版本中增加了 EvokerFangs 召唤物判定
     *
     * @param e 伤害事件
     * @return 实体实例
     */
    @Nullable
    public static LivingEntity getAttacker(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof LivingEntity) {
            return (LivingEntity) e.getDamager();
        } else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof LivingEntity) {
            return (LivingEntity) ((Projectile) e.getDamager()).getShooter();
        } else if (Version.isAfter(Version.v1_11) && e.getDamager() instanceof EvokerFangs) {
            return ((EvokerFangs) e.getDamager()).getOwner();
        } else {
            return null;
        }
    }

    /**
     * 执行控制台命令
     *
     * @param command 命令
     * @return 执行结果
     */
    public static boolean dispatchCommand(String command) {
        return dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    /**
     * 执行命令
     *
     * @param sender  目标
     * @param command 命令
     * @return 执行结果
     */
    public static boolean dispatchCommand(CommandSender sender, String command) {
        try {
            if ((sender instanceof Player)) {
                PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent((Player) sender, "/" + command);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled() || Strings.isBlank(e.getMessage()) || !e.getMessage().startsWith("/")) {
                    return false;
                }
                return Bukkit.dispatchCommand(e.getPlayer(), e.getMessage().substring(1));
            } else {
                ServerCommandEvent e = new ServerCommandEvent(sender, command);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled() || Strings.isBlank(e.getCommand())) {
                    return false;
                }
                return Bukkit.dispatchCommand(e.getSender(), e.getCommand());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 执行命令
     *
     * @param sender  目标
     * @param command 命令
     * @param op      是否跳过权限判定
     * @return 执行结果
     */
    public static boolean dispatchCommand(CommandSender sender, String command, boolean op) {
        if (op) {
            boolean r = false;
            boolean isOp = sender.isOp();
            sender.setOp(true);
            try {
                r = dispatchCommand(sender, command);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            sender.setOp(isOp);
            return r;
        } else {
            return dispatchCommand(sender, command);
        }
    }

    /**
     * 模拟玩家丢弃物品
     *
     * @param player    玩家
     * @param itemStack 物品
     * @return 物品实例
     */
    @NotNull
    public static Item dropItem(Player player, ItemStack itemStack) {
        return dropItem(player, itemStack, 0.0, 0.4);
    }

    /**
     * 模拟玩家丢弃物品
     *
     * @param player       玩家
     * @param itemStack    物品
     * @param bulletSpread 偏移
     * @param radius       半径
     * @return 物品实例
     */
    @NotNull
    public static Item dropItem(Player player, ItemStack itemStack, double bulletSpread, double radius) {
        Location location = player.getLocation().add(0.0D, 1.5D, 0.0D);
        Item item = player.getWorld().dropItem(location, itemStack);
        double yaw = Math.toRadians(-player.getLocation().getYaw() - 90.0F);
        double pitch = Math.toRadians(-player.getLocation().getPitch());
        double x;
        double y;
        double z;
        final double x1 = Math.cos(pitch) * Math.cos(yaw);
        final double v = -Math.sin(yaw) * Math.cos(pitch);
        if (bulletSpread > 0.0D) {
            double[] spread = new double[]{1.0D, 1.0D, 1.0D};
            IntStream.range(0, 3).forEach((t) -> {
                spread[t] = (Numbers.getRandom().nextDouble() - Numbers.getRandom().nextDouble()) * bulletSpread * 0.1D;
            });
            x = x1 + spread[0];
            y = Math.sin(pitch) + spread[1];
            z = v + spread[2];
        } else {
            x = x1;
            y = Math.sin(pitch);
            z = v;
        }

        Vector dirVel = new Vector(x, y, z);
        item.setVelocity(dirVel.normalize().multiply(radius));
        return item;
    }

    /**
     * 将实体推向某个坐标
     *
     * @param entity   实体
     * @param to       坐标
     * @param velocity 力度
     */
    public static void pushEntity(Entity entity, Location to, double velocity) {
        Location from = entity.getLocation();
        Vector test = to.clone().subtract(from).toVector();
        double elevation = test.getY();
        Double launchAngle = calculateLaunchAngle(from, to, velocity, elevation, 20.0D);
        double distance = Math.sqrt(Math.pow(test.getX(), 2.0D) + Math.pow(test.getZ(), 2.0D));
        if (distance != 0.0D) {
            if (launchAngle == null) {
                launchAngle = Math.atan((40.0D * elevation + Math.pow(velocity, 2.0D)) / (40.0D * elevation + 2.0D * Math.pow(velocity, 2.0D)));
            }
            double hangTime = calculateHangTime(launchAngle, velocity, elevation, 20.0D);
            test.setY(Math.tan(launchAngle) * distance);
            test = normalizeVector(test);
            Vector noise = Vector.getRandom();
            noise = noise.multiply(0.1D);
            test.add(noise);
            velocity = velocity + 1.188D * Math.pow(hangTime, 2.0D) + (Numbers.getRandom().nextDouble() - 0.8D) / 2.0D;
            test = test.multiply(velocity / 20.0D);
            entity.setVelocity(test);
        }
    }

    private static double calculateHangTime(double launchAngle, double v, double elev, double g) {
        double a = v * Math.sin(launchAngle);
        double b = -2.0D * g * elev;
        return Math.pow(a, 2.0D) + b < 0.0D ? 0.0D : (a + Math.sqrt(Math.pow(a, 2.0D) + b)) / g;
    }

    private static Vector normalizeVector(Vector victor) {
        double mag = Math.sqrt(Math.pow(victor.getX(), 2.0D) + Math.pow(victor.getY(), 2.0D) + Math.pow(victor.getZ(), 2.0D));
        return mag != 0.0D ? victor.multiply(1.0D / mag) : victor.multiply(0);
    }

    private static Double calculateLaunchAngle(Location from, Location to, double v, double elevation, double g) {
        Vector vector = from.clone().subtract(to).toVector();
        double distance = Math.sqrt(Math.pow(vector.getX(), 2.0D) + Math.pow(vector.getZ(), 2.0D));
        double v2 = Math.pow(v, 2.0D);
        double v4 = Math.pow(v, 4.0D);
        double check = g * (g * Math.pow(distance, 2.0D) + 2.0D * elevation * v2);
        return v4 < check ? null : Math.atan((v2 - Math.sqrt(v4 - check)) / (g * distance));
    }

    public interface ChatInput {

        default String quit() {
            return "(?i)quit|cancel|exit";
        }

        default void head() {
        }

        default void cancel() {
        }

        boolean onChat(@NotNull String message);
    }
}

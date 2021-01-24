package io.izzel.taboolib.module.locale.type;

import com.google.common.collect.Maps;
import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.locale.TLocaleSerialize;
import io.izzel.taboolib.module.tellraw.TellrawJson;
import io.izzel.taboolib.util.Strings;
import io.izzel.taboolib.util.book.BookFormatter;
import io.izzel.taboolib.util.book.builder.BookBuilder;
import io.izzel.taboolib.util.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author sky
 * @since 2018-05-27 0:05
 */
@ThreadSafe
@SerializableAs("BOOK")
public class TLocaleBook extends TLocaleSerialize {

    /*
        BookTest:
        - ==: BOOK
          pages:
            0:
            - '第一页内容'
            - '[ <变量1@page-1> ]'
            1:
            - '第二页内容'
            - '[ <变量2@page-2> ]'
          args:
            page-1:
              hover: '展示内容1'
              command: '/say %player_name% NB1'
            page-2:
              hover: '展示内容2'
              suggest: '/say %player_name% NB2'
     */

    private final List<TellrawJson> pages;
    private final Map<String, Object> map;
    private final boolean papi;

    public TLocaleBook(List<TellrawJson> pages, Map<String, Object> map, boolean papi) {
        this.pages = pages;
        this.map = map;
        this.papi = papi;
    }

    @Override
    public Map<String, Object> serialize() {
        return Maps.newHashMap(map);
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (!(sender instanceof Player)) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                BookBuilder bookBuilder = BookFormatter.writtenBook();
                pages.stream().map(jsonPage -> format(jsonPage, sender, args)).map(ComponentSerializer::parse).forEach(bookBuilder::addPages);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        BookFormatter.forceOpen((Player) sender, bookBuilder.build());
                    }
                }.runTask(TabooLib.getPlugin());
            }
        }.runTaskAsynchronously(TabooLib.getPlugin());
    }

    private String format(TellrawJson jsonPage, CommandSender sender, String[] args) {
        return papi ? TLocale.Translate.setPlaceholders(sender, Strings.replaceWithOrder(jsonPage.toRawMessage(), args)) : Strings.replaceWithOrder(jsonPage.toRawMessage(), args);
    }

    public static TLocaleBook valueOf(Map<String, Object> map) {
        Map<String, Object> pages = map.containsKey("pages") ? (Map<String, Object>) map.get("pages") : new HashMap<>();
        Map<String, Object> section = map.containsKey("args") ? (Map<String, Object>) map.get("args") : new HashMap<>();
        List<TellrawJson> pageJsonList = pages.values().stream().map(page -> TLocaleJson.formatJson(section, page, TellrawJson.create())).collect(Collectors.toList());
        return new TLocaleBook(pageJsonList, map, isPlaceholderEnabled(map));
    }
}

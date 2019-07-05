package io.izzel.taboolib.module.tellraw;

import io.izzel.taboolib.locale.TLocale;
import io.izzel.taboolib.util.ArrayUtil;
import io.izzel.taboolib.util.Strings;
import io.izzel.taboolib.util.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import protocolsupport.api.ProtocolSupportAPI;
import us.myles.ViaVersion.api.Via;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2018-05-26 14:42json
 * @BuilderLevel 1.2
 */
public class TellrawJson {

    private List<BaseComponent> components = new ArrayList<>();
    private List<BaseComponent> componentsLatest = new ArrayList<>();
    private Map<String, String[]> itemTag = new HashMap<>();
    private List<String> nbtWhitelist = ArrayUtil.asList(
            // 附魔
            "ench",
            // 附魔书
            "StoredEnchantments",
            // 展示
            "display",
            // 属性
            "AttributeModifiers",
            // 药水
            "Potion",
            // 特殊药水
            "CustomPotionEffects",
            // 隐藏标签
            "HideFlags",
            // 方块标签
            "BlockEntityTag"
    );

    TellrawJson() {
    }

    public static TellrawJson create() {
        return new TellrawJson();
    }

    public void broadcast() {
        Bukkit.getOnlinePlayers().forEach(player -> send(player, new String[0]));
    }

    public void broadcast(String... args) {
        Bukkit.getOnlinePlayers().forEach(player -> send(player, args));
    }

    public void send(CommandSender sender) {
        send(sender, new String[0]);
    }

    public void send(CommandSender sender, String... args) {
        if (sender instanceof Player) {
            TLocale.Tellraw.send(sender, Strings.replaceWithOrder(toRawMessage((Player) sender), args));
        } else {
            TLocale.Tellraw.send(sender, Strings.replaceWithOrder(toRawMessage(), args));
        }
    }

    public String toRawMessage() {
        return ComponentSerializer.toString(getComponentsAll());
    }

    public String toRawMessage(TellrawVersion version) {
        String rawMessage = toRawMessage();
        if (version == TellrawVersion.CURRENT_VERSION) {
            return rawMessage;
        }
        for (Map.Entry<String, String[]> stringEntry : itemTag.entrySet()) {
            rawMessage = rawMessage.replace(stringEntry.getKey(), version == TellrawVersion.HIGH_VERSION ? stringEntry.getValue()[1] : stringEntry.getValue()[0]);
        }
        return rawMessage;
    }

    public String toRawMessage(Player player) {
        // ViaVersion support!
        if (TellrawCreator.isViaVersionLoaded()) {
            return toRawMessage(Via.getAPI().getPlayerVersion(player) > 316 ? TellrawVersion.HIGH_VERSION : TellrawVersion.LOW_VERSION);
        }
        // ProtocolSupport support!
        else if (TellrawCreator.isProtocolSupportLoaded()) {
            return toRawMessage(ProtocolSupportAPI.getProtocolVersion(player).getId() > 316 ? TellrawVersion.HIGH_VERSION : TellrawVersion.LOW_VERSION);
        }
        return toRawMessage();
    }

    public String toLegacyText() {
        return TextComponent.toLegacyText(getComponentsAll());
    }

    public TellrawJson newLine() {
        return append("\n");
    }

    public TellrawJson append(String text) {
        appendComponents();
        componentsLatest.addAll(Arrays.asList(TextComponent.fromLegacyText(text)));
        return this;
    }

    public TellrawJson append(TellrawJson json) {
        appendComponents();
        componentsLatest.addAll(Arrays.asList(json.getComponentsAll()));
        itemTag.putAll(json.itemTag);
        return this;
    }

    public TellrawJson hoverText(String text) {
        getLatestComponent().forEach(component -> component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create())));
        return this;
    }

    public TellrawJson hoverItem(ItemStack itemStack) {
        return hoverItem(itemStack, true);
    }

    public TellrawJson hoverItem(ItemStack itemStack, boolean supportVersion) {
        itemStack = TellrawCreator.getAbstractTellraw().optimizeNBT(itemStack, nbtWhitelist);
        BaseComponent[] itemComponentCurrentVersion = new ComponentBuilder(TellrawCreator.getAbstractTellraw().getItemComponent(itemStack, TellrawVersion.CURRENT_VERSION)).create();
        getLatestComponent().forEach(component -> component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, itemComponentCurrentVersion)));
        if (supportVersion) {
            itemTag.put(ComponentSerializer.toString(itemComponentCurrentVersion), new String[] {
                    ComponentSerializer.toString(new ComponentBuilder(TellrawCreator.getAbstractTellraw().getItemComponent(itemStack, TellrawVersion.LOW_VERSION)).create()),
                    ComponentSerializer.toString(new ComponentBuilder(TellrawCreator.getAbstractTellraw().getItemComponent(itemStack, TellrawVersion.HIGH_VERSION)).create())
            });
        }
        return this;
    }

    public TellrawJson clickCommand(String command) {
        getLatestComponent().forEach(component -> component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
        return this;
    }

    public TellrawJson clickSuggest(String command) {
        getLatestComponent().forEach(component -> component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)));
        return this;
    }

    public TellrawJson clickOpenURL(String url) {
        getLatestComponent().forEach(component -> component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
        return this;
    }

    public TellrawJson clickChangePage(int page) {
        getLatestComponent().forEach(component -> component.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, String.valueOf(page))));
        return this;
    }

    public BaseComponent[] getComponentsAll() {
        List<BaseComponent> components = this.components.stream().filter(component -> !(component instanceof TextComponent) || !((TextComponent) component).getText().isEmpty()).collect(Collectors.toList());
        this.componentsLatest.stream().filter(component -> !(component instanceof TextComponent) || !((TextComponent) component).getText().isEmpty()).forEach(components::add);
        return components.toArray(new BaseComponent[0]);
    }

    @Deprecated
    public String getItemComponent(ItemStack item) {
        return TellrawCreator.getAbstractTellraw().getItemComponent(item);
    }

    // *********************************
    //
    //         Private Methods
    //
    // *********************************

    private List<BaseComponent> getLatestComponent() {
        return componentsLatest;
    }

    private void setLatestComponent(BaseComponent... component) {
        componentsLatest.addAll(Arrays.asList(component));
    }

    private void appendComponents() {
        components.addAll(componentsLatest);
        componentsLatest.clear();
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public void setComponents(BaseComponent[] components) {
        this.components = Arrays.asList(components);
    }

    public BaseComponent[] getComponents() {
        return components.toArray(new BaseComponent[0]);
    }

    public List<BaseComponent> getComponentsLatest() {
        return componentsLatest;
    }

    public Map<String, String[]> getItemTag() {
        return itemTag;
    }

    public List<String> getNBTWhitelist() {
        return nbtWhitelist;
    }
}

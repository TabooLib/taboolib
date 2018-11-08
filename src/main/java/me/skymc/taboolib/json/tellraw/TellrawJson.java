package me.skymc.taboolib.json.tellraw;

import com.ilummc.tlib.bungee.api.chat.*;
import com.ilummc.tlib.bungee.chat.ComponentSerializer;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.string.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import protocolsupport.api.ProtocolSupportAPI;
import us.myles.ViaVersion.api.Via;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private int bukkitVersion = TabooLib.getVersionNumber();

    TellrawJson() {
    }

    public static TellrawJson create() {
        return new TellrawJson();
    }

    public void send(CommandSender sender) {
        send(sender, new String[0]);
    }

    public void send(CommandSender sender, String... args) {
        if (sender instanceof Player) {
            if (TellrawCreator.isViaVersionLoaded()) {
                TLocale.Tellraw.send(sender, Strings.replaceWithOrder(toRawMessage(Via.getAPI().getPlayerVersion(sender) > 316 ? TellrawVersion.HIGH_VERSION : TellrawVersion.LOW_VERSION), args));
            } else if (TellrawCreator.isProtocolSupportLoaded()) {
                TLocale.Tellraw.send(sender, Strings.replaceWithOrder(toRawMessage(ProtocolSupportAPI.getProtocolVersion((Player) sender).getId() > 316 ? TellrawVersion.HIGH_VERSION : TellrawVersion.LOW_VERSION), args));
            } else {
                TLocale.Tellraw.send(sender, Strings.replaceWithOrder(toRawMessage(), args));
            }
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

    public String toLegacyText() {
        return TextComponent.toLegacyText(getComponentsAll());
    }

    public TellrawJson newLine() {
        return append("\n");
    }

    public TellrawJson append(String text) {
        appendComponents();
        componentsLatest.addAll(ArrayUtils.asList(TextComponent.fromLegacyText(text)));
        return this;
    }

    public TellrawJson append(TellrawJson json) {
        appendComponents();
        componentsLatest.addAll(ArrayUtils.asList(json.getComponentsAll()));
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

    // *********************************
    //
    //         Private Methods
    //
    // *********************************

    private List<BaseComponent> getLatestComponent() {
        return componentsLatest;
    }

    private void setLatestComponent(BaseComponent... component) {
        componentsLatest.addAll(ArrayUtils.asList(component));
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
        this.components = ArrayUtils.asList(components);
    }

    public BaseComponent[] getComponents() {
        return components.toArray(new BaseComponent[0]);
    }
}

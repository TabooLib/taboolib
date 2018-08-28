package me.skymc.taboolib.json.tellraw;

import com.ilummc.tlib.bungee.api.chat.*;
import com.ilummc.tlib.bungee.chat.ComponentSerializer;
import com.ilummc.tlib.logger.TLogger;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.methods.ReflectionUtils;
import me.skymc.taboolib.nms.NMSUtils;
import me.skymc.taboolib.string.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author sky
 * @Since 2018-05-26 14:42json
 * @BuilderLevel 1.2
 */
public class TellrawJson {

    private List<BaseComponent> components = new ArrayList<>();
    private List<BaseComponent> componentsLatest = new ArrayList<>();
    private static final Class<?> craftItemStackClazz = NMSUtils.getOBCClass("inventory.CraftItemStack");
    private static final Class<?> nmsItemStackClazz = NMSUtils.getNMSClass("ItemStack");
    private static final Class<?> nbtTagCompoundClazz = NMSUtils.getNMSClass("NBTTagCompound");
    private static final String INVALID_ITEM = "{id:stone,tag:{display:{Name:§c* Invalid ItemStack *}}}";

    TellrawJson() {
    }

    public static TellrawJson create() {
        return new TellrawJson();
    }

    public void send(CommandSender sender) {
        TLocale.Tellraw.send(sender, toRawMessage());
    }

    public String toRawMessage() {
        return ComponentSerializer.toString(getComponentsAll());
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
        return this;
    }

    public TellrawJson hoverText(String text) {
        getLatestComponent().forEach(component -> component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create())));
        return this;
    }

    public TellrawJson hoverItem(ItemStack itemStack) {
        getLatestComponent().forEach(component -> component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(getItemComponent(itemStack)).create())));
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

    public String getItemComponent(ItemStack itemStack) {
        try {
            Method asNMSCopyMethod = ReflectionUtils.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);
            Method saveNmsItemStackMethod = ReflectionUtils.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);
            Object nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            Object nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            return saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj).toString();
        } catch (Throwable t) {
            TLogger.getGlobalLogger().error("failed to serialize bukkit item to nms item: " + t.toString());
            return INVALID_ITEM;
        }
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

    public BaseComponent[] getComponentsAll() {
        List<BaseComponent> components = new ArrayList<>(this.components);
        components.addAll(componentsLatest);
        return components.toArray(new BaseComponent[0]);
    }
}

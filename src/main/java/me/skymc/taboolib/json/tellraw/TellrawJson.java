package me.skymc.taboolib.json.tellraw;

import com.ilummc.tlib.bungee.api.chat.*;
import com.ilummc.tlib.bungee.chat.ComponentSerializer;
import com.ilummc.tlib.logger.TLogger;
import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.inventory.ItemUtils;
import me.skymc.taboolib.methods.ReflectionUtils;
import me.skymc.taboolib.nms.NMSUtils;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.string.ArrayUtils;
import me.skymc.taboolib.string.VariableFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Author sky
 * @Since 2018-05-26 14:42json
 */
public class TellrawJson {

    private BaseComponent[] components = TextComponent.fromLegacyText("");
    private final Class<?> craftItemStackClazz = NMSUtils.getOBCClass("inventory.CraftItemStack");
    private final Class<?> nmsItemStackClazz = NMSUtils.getNMSClass("ItemStack");
    private final Class<?> nbtTagCompoundClazz = NMSUtils.getNMSClass("NBTTagCompound");
    private final String INVALID_ITEM = "{id:stone,tag:{display:{Name:§c* Invalid ItemStack *}}}";

    TellrawJson() {
    }

    public static TellrawJson create() {
        return new TellrawJson();
    }

    public String toRawMessage() {
        return ComponentSerializer.toString(components);
    }

    public String toLegacyText() {
        return TextComponent.toLegacyText(components);
    }

    public TellrawJson newLine() {
        return append("\n");
    }

    public TellrawJson append(String text) {
        Arrays.stream(TextComponent.fromLegacyText(text)).forEach(component -> this.components = ArrayUtils.arrayAppend(this.components, component));
        return this;
    }

    public TellrawJson append(TellrawJson json) {
        BaseComponent[] newArray = new BaseComponent[components.length + json.components.length];
        System.arraycopy(components, 0, newArray, 0, components.length);
        System.arraycopy(json.components, 0, newArray, components.length, json.components.length);
        components = newArray;
        return this;
    }

    public TellrawJson hoverText(String text) {
        getLatestComponent().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create()));
        return this;
    }

    public TellrawJson hoverItem(ItemStack itemStack) {
        getLatestComponent().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(getItemComponent(itemStack)).create()));
        return this;
    }

    public TellrawJson clickCommand(String command) {
        getLatestComponent().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return this;
    }

    public TellrawJson clickSuggest(String command) {
        getLatestComponent().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        return this;
    }

    public TellrawJson clickOpenURL(String url) {
        getLatestComponent().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        return this;
    }

    public TellrawJson clickChangePage(int page) {
        getLatestComponent().setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, String.valueOf(page)));
        return this;
    }

    public void send(CommandSender sender) {
        TLocale.Tellraw.send(sender, toRawMessage());
    }

    public String getItemComponent(ItemStack itemStack) {
        try {
            Method asNMSCopyMethod = ReflectionUtils.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);
            Method saveNmsItemStackMethod = ReflectionUtils.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);
            Object nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            Object nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            return saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj).toString();
        } catch (Throwable t) {
            TLogger.getGlobalLogger().error("failed to serialize itemstack to nms item: " + t.toString());
            return INVALID_ITEM;
        }
    }

    // *********************************
    //
    //         Private Methods
    //
    // *********************************

    private BaseComponent getLatestComponent() {
        return components[components.length - 1];
    }

    private void setLatestComponent(BaseComponent component) {
        components[components.length - 1] = component;
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public BaseComponent[] getComponents() {
        return components;
    }

    public void setComponents(BaseComponent[] components) {
        this.components = components;
    }

}

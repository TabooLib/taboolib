package me.skymc.taboolib.bookformatter.action;

import me.skymc.taboolib.bookformatter.BookAchievement;
import me.skymc.taboolib.bookformatter.BookReflection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Achievement;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * @author sky
 * @since 2018-03-08 22:38:51
 */
@SuppressWarnings("deprecation")
public interface HoverAction {

    /**
     * Get the Chat-Component action
     *
     * @return the Chat-Component action
     */
    HoverEvent.Action action();

    /**
     * The value paired to the action
     *
     * @return the value paired tot the action
     */
    BaseComponent[] value();


    /**
     * Creates a show_text action: when the component is hovered the text used as parameter will be displayed
     *
     * @param text the text to display
     * @return a new HoverAction instance
     */
    static HoverAction showText(BaseComponent... text) {
        return new SimpleHoverAction(HoverEvent.Action.SHOW_TEXT, text);
    }

    /**
     * Creates a show_text action: when the component is hovered the text used as parameter will be displayed
     *
     * @param text the text to display
     * @return a new HoverAction instance
     */
    static HoverAction showText(String text) {
        return new SimpleHoverAction(HoverEvent.Action.SHOW_TEXT, new TextComponent(text));
    }

    /**
     * Creates a show_item action: when the component is hovered some item information will be displayed
     *
     * @param item a component array representing item to display
     * @return a new HoverAction instance
     */
    static HoverAction showItem(BaseComponent... item) {
        return new SimpleHoverAction(HoverEvent.Action.SHOW_ITEM, item);
    }

    /**
     * Creates a show_item action: when the component is hovered some item information will be displayed
     *
     * @param item the item to display
     * @return a new HoverAction instance
     */
    static HoverAction showItem(ItemStack item) {
        return new SimpleHoverAction(HoverEvent.Action.SHOW_ITEM, BookReflection.itemToComponents(item));
    }

    /**
     * Creates a show_entity action: when the component is hovered some entity information will be displayed
     *
     * @param entity a component array representing the item to display
     * @return a new HoverAction instance
     */
    static HoverAction showEntity(BaseComponent... entity) {
        return new SimpleHoverAction(HoverEvent.Action.SHOW_ENTITY, entity);
    }

    /**
     * Creates a show_entity action: when the component is hovered some entity information will be displayed
     *
     * @param uuid the entity's UniqueId
     * @param type the entity's type
     * @param name the entity's name
     * @return a new HoverAction instance
     */
    static HoverAction showEntity(UUID uuid, String type, String name) {
        return new SimpleHoverAction(HoverEvent.Action.SHOW_ENTITY,
                BookReflection.jsonToComponents(
                        "{id:\"" + uuid + "\",type:\"" + type + "\"name:\"" + name + "\"}"
                )
        );
    }

    /**
     * Creates a show_entity action: when the component is hovered some entity information will be displayed
     *
     * @param entity the item to display
     * @return a new HoverAction instance
     */
    static HoverAction showEntity(Entity entity) {
        return showEntity(entity.getUniqueId(), entity.getType().getName(), entity.getName());
    }

    /**
     * Creates a show_achievement action: when the component is hovered the achievement information will be displayed
     *
     * @param achievementId the id of the achievement to display
     * @return a new HoverAction instance
     */
    static HoverAction showAchievement(String achievementId) {
        return new SimpleHoverAction(HoverEvent.Action.SHOW_ACHIEVEMENT, new TextComponent("achievement." + achievementId));
    }

    /**
     * Creates a show_achievement action: when the component is hovered the achievement information will be displayed
     *
     * @param achievement the achievement to display
     * @return a new HoverAction instance
     */
    static HoverAction showAchievement(Achievement achievement) {
        return showAchievement(BookAchievement.toId(achievement));
    }

    /**
     * Creates a show_achievement action: when the component is hovered the statistic information will be displayed
     *
     * @param statisticId the id of the statistic to display
     * @return a new HoverAction instance
     */
    static HoverAction showStatistic(String statisticId) {
        return new SimpleHoverAction(HoverEvent.Action.SHOW_ACHIEVEMENT, new TextComponent("statistic." + statisticId));
    }

    class SimpleHoverAction implements HoverAction {
        private final HoverEvent.Action action;
        private final BaseComponent[] value;

        public SimpleHoverAction(HoverEvent.Action action, BaseComponent... value) {
            this.action = action;
            this.value = value;
        }

        public HoverEvent.Action getAction() {
            return action;
        }

        public BaseComponent[] getValue() {
            return value;
        }

        @Override
        public HoverEvent.Action action() {
            return null;
        }

        @Override
        public BaseComponent[] value() {
            return new BaseComponent[0];
        }
    }
}
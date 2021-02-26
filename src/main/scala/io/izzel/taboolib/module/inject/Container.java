package io.izzel.taboolib.module.inject;

import org.bukkit.entity.Player;

/**
 * @author sky
 */
public class Container {

    private final Object container;
    private final boolean uniqueId;

    public Container(Object container, boolean uniqueId) {
        this.container = container;
        this.uniqueId = uniqueId;
    }

    public boolean isInstanceOf(Class<?> clazz) {
        return clazz.isInstance(container);
    }

    public Object namespace(Player player) {
        return uniqueId ? player.getUniqueId() : player.getName();
    }

    public <T> T cast() {
        return (T) container;
    }

    @Override
    public String toString() {
        return "Container{" +
                "container=" + container +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
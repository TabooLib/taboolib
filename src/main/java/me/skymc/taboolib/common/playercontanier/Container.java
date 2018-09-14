package me.skymc.taboolib.common.playercontanier;

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

    public Object getContainer() {
        return container;
    }

    public boolean isUniqueId() {
        return uniqueId;
    }
}
package io.izzel.taboolib.module.inject;

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

    @SuppressWarnings("unchecked")
    public <T> T as() {
        return (T) container;
    }

    public Object getContainer() {
        return container;
    }

    public boolean isUniqueId() {
        return uniqueId;
    }
}
package io.izzel.taboolib.module.lite;

import org.objectweb.asm.commons.Remapper;

public class VersionRemapper extends Remapper {

    private final SimpleVersionControl versionControl;

    public VersionRemapper(SimpleVersionControl versionControl) {
        this.versionControl = versionControl;
    }

    @Override
    public String map(String internalName) {
        return versionControl.replace(internalName);
    }
}

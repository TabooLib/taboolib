package io.izzel.taboolib.util.asm;

import org.objectweb.asm.commons.Remapper;

public class VersionRemapper extends Remapper {

    private final AsmVersionControl versionControl;

    public VersionRemapper(AsmVersionControl versionControl) {
        this.versionControl = versionControl;
    }

    @Override
    public String map(String internalName) {
        return versionControl.replace(internalName);
    }
}

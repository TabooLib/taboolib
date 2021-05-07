package io.izzel.taboolib.module.nms;

import org.jetbrains.annotations.NotNull;

/**
 * TabooLib
 * io.izzel.taboolib.module.nms.NMSFactory
 *
 * @author sky
 * @since 2021/4/29 2:07 下午
 */
public class NMSFactory {

    public static final NMSFactory INSTANCE = new NMSFactory();

    @NotNull
    public NMS generic() {
        return NMS.handle();
    }

    @NotNull
    public ink.ptms.navigation.pathfinder.bukkit.NMS boundingBox() {
        return ink.ptms.navigation.pathfinder.bukkit.NMS.INSTANCE;
    }
}

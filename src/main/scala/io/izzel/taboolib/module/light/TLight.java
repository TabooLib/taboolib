package io.izzel.taboolib.module.light;

import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.nms.impl.Type;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * 光照工具
 *
 * @Author sky
 * @Since 2020-04-02 18:10
 */
public class TLight {

    public static boolean create(Block block, Type lightType, int lightLevel) {
        if (NMS.handle().getRawLightLevel(block, lightType) > lightLevel) {
            NMS.handle().deleteLight(block, lightType);
        }
        boolean r = NMS.handle().createLight(block, lightType, lightLevel);
        NMS.handle().update(block.getChunk());
        return r;
    }

    public static boolean delete(Block block, Type lightType) {
        boolean r = NMS.handle().deleteLight(block, lightType);
        NMS.handle().update(block.getChunk());
        return r;
    }

    public static boolean create(Location location, Type lightType, int lightLevel) {
        if (NMS.handle().getRawLightLevel(location.getBlock(), lightType) > lightLevel) {
            deleteLight(location, lightType);
        }
        boolean r = NMS.handle().createLight(location.getBlock(), lightType, lightLevel);
        NMS.handle().update(location.getChunk());
        return r;
    }

    public static boolean delete(Location location, Type lightType) {
        boolean r = NMS.handle().deleteLight(location.getBlock(), lightType);
        NMS.handle().update(location.getChunk());
        return r;
    }

    public static boolean createLight(Location location, Type lightType, int lightLevel) {
        if (NMS.handle().getRawLightLevel(location.getBlock(), lightType) > lightLevel) {
            deleteLight(location, lightType);
        }
        return NMS.handle().createLight(location.getBlock(), lightType, lightLevel);
    }

    public static boolean deleteLight(Location location, Type lightType) {
        return NMS.handle().deleteLight(location.getBlock(), lightType);
    }

}

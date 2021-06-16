package taboolib.module.porticus.bukkitside;

import taboolib.module.porticus.API;
import taboolib.module.porticus.APIType;
import taboolib.module.porticus.PorticusMission;

import java.util.UUID;

/**
 * Porticus
 * taboolib.module.porticus.bukkitside.PorticusAPI
 *
 * @author bkm016
 * @since 2020/10/15 9:48 下午
 */
public class PorticusAPI extends API {

    private static final PorticusListener listener = new PorticusListener();

    @Override
    public APIType getType() {
        return APIType.CLIENT;
    }

    @Override
    public PorticusMission createMission() {
        return new MissionBukkit();
    }

    @Override
    public PorticusMission createMission(UUID uid) {
        return new MissionBukkit(uid);
    }
}

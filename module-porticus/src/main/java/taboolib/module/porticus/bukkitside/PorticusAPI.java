package taboolib.module.porticus.bukkitside;

import org.jetbrains.annotations.NotNull;
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

    private final PorticusListener listener = new PorticusListener();

    @NotNull
    @Override
    public APIType getType() {
        return APIType.CLIENT;
    }

    @NotNull
    @Override
    public PorticusMission createMission() {
        return new MissionBukkit();
    }

    @NotNull
    @Override
    public PorticusMission createMission(@NotNull UUID uid) {
        return new MissionBukkit(uid);
    }
}

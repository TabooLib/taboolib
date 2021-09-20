package taboolib.module.porticus.bungeeside;

import org.jetbrains.annotations.NotNull;
import taboolib.module.porticus.API;
import taboolib.module.porticus.APIType;
import taboolib.module.porticus.PorticusMission;

import java.util.UUID;

/**
 * Porticus
 * taboolib.module.porticus.bungeeside.PorticusAPI
 *
 * @author bkm016
 * @since 2020/10/15 9:50 下午
 */
public class PorticusAPI extends API {

    private static final PorticusListener listener = new PorticusListener();

    @NotNull
    @Override
    public APIType getType() {
        return APIType.SERVER;
    }

    @NotNull
    @Override
    public PorticusMission createMission() {
        return new MissionBungee();
    }

    @NotNull
    @Override
    public PorticusMission createMission(@NotNull UUID uid) {
        return new MissionBungee(uid);
    }
}

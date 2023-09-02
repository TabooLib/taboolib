package taboolib.expansion.folialib.Enum;


public enum ServerType {

    FOLIA("io.papermc.paper.threadedregions.RegionizedServerInitEvent"),
    BUKKIT("org.bukkit.Bukkit");
    private final String className;
    ServerType(String className){
        this.className = className;
    }
    public static ServerType getServerType(){
        try {
            Class.forName(FOLIA.className);
            return FOLIA;
        } catch (ClassNotFoundException e) {
            return BUKKIT;
        }
    }
}

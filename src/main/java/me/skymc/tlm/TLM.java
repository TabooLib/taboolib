package me.skymc.tlm;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.fileutils.ConfigUtils;
import me.skymc.taboolib.string.language2.Language2;
import me.skymc.tlm.module.TabooLibraryModule;
import me.skymc.tlm.module.sub.ModuleCommandChanger;
import me.skymc.tlm.module.sub.ModuleInventorySave;
import me.skymc.tlm.module.sub.ModuleKits;
import me.skymc.tlm.module.sub.ModuleTimeCycle;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author sky
 * @since 2018年2月17日 下午10:28:05
 */
public class TLM {

    private static TLM inst = null;

    private FileConfiguration config;

    private Language2 language;

    /**
     * 构造方法
     */
    private TLM() {
        // 重载配置文件
        reloadConfig();

        // 载入模块
        if (isEnableModule("TimeCycle")) {
            TabooLibraryModule.getInst().register(new ModuleTimeCycle());
        }
        if (isEnableModule("Kits")) {
            TabooLibraryModule.getInst().register(new ModuleKits());
        }
        if (isEnableModule("CommandChanger")) {
            TabooLibraryModule.getInst().register(new ModuleCommandChanger());
        }
        if (isEnableModule("InventorySave")) {
            TabooLibraryModule.getInst().register(new ModuleInventorySave());
        }

        // 载入模块
        TabooLibraryModule.getInst().loadModules();
        // 提示
        TLocale.Logger.info("TABOOLIB-MODULE.SUCCESS-LOADED", String.valueOf(TabooLibraryModule.getInst().getSize()));
    }

    /**
     * 获取 TLM 对象
     *
     * @return TLM
     */
    public static TLM getInst() {
        if (inst == null) {
            synchronized (TLM.class) {
                if (inst == null) {
                    inst = new TLM();
                }
            }
        }
        return inst;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public Language2 getLanguage() {
        return language;
    }

    /**
     * 载入配置文件
     */
    public void reloadConfig() {
        config = ConfigUtils.saveDefaultConfig(Main.getInst(), "module.yml");
        language = new Language2(config.getString("Language"), Main.getInst());
    }

    /**
     * 模块是否启用
     *
     * @param name 名称
     * @return boolean
     */
    public boolean isEnableModule(String name) {
        return config.getStringList("EnableModule").contains(name);
    }

    public void loadedFall(String moduleName, String result, String location) {
        TLocale.Logger.error("TABOOLIB-MODULE.FALL-LOADED", moduleName, result, location);
    }

    public void runtimeFall(String moduleName, String result, String location) {
        TLocale.Logger.error("TABOOLIB-MODULE.FALL-RUNTIME", moduleName, result, location);
    }
}

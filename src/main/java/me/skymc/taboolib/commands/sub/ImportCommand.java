package me.skymc.taboolib.commands.sub;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.Main;
import me.skymc.taboolib.Main.StorageType;
import me.skymc.taboolib.commands.SubCommand;
import me.skymc.taboolib.fileutils.ConfigUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Objects;

public class ImportCommand extends SubCommand {

	public ImportCommand(CommandSender sender, String[] args) {
		super(sender, args);
		
		if (isPlayer()) {
            TLocale.sendTo(sender, "COMMANDS.GLOBAL.ONLY-PLAYER");
            return;
        }

        if (Main.getStorageType() == StorageType.LOCAL) {
            TLocale.Logger.warn("COMMANDS.GLOBAL.ONLY-STORAGE-SQL");
            return;
        }

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.IMPORTDATA.CLEARING");
        Main.getConnection().truncateTable(Main.getTablePrefix() + "_playerdata");

        if (!Main.getPlayerDataFolder().exists()) {
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.IMPORTDATA.EMPTYDATA");
            return;
        }

        int size = Objects.requireNonNull(Main.getPlayerDataFolder().listFiles()).length;
        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.IMPORTDATA.IMPORTING-START", String.valueOf(size));

        int loop = 1;
        for (File file : Objects.requireNonNull(Main.getPlayerDataFolder().listFiles())) {
            Main.getConnection().intoValue(Main.getTablePrefix() + "_playerdata", file.getName().replace(".yml", ""), ConfigUtils.encodeYAML(YamlConfiguration.loadConfiguration(file)));
            TLocale.sendTo(sender, "COMMANDS.TABOOLIB.IMPORTDATA.IMPORTING-PROGRESS", file.getName().replace(".yml", ""), String.valueOf(loop), String.valueOf(size));
            loop++;
        }

        TLocale.sendTo(sender, "COMMANDS.TABOOLIB.IMPORTDATA.SUCCESS");
	}

}

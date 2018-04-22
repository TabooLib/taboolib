package me.skymc.taboolib.fileutils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.bukkit.plugin.Plugin;

import me.skymc.taboolib.Main;
import me.skymc.taboolib.other.DateUtils;

@Deprecated
public class LogUtils {
    
    public static void Log(String s, String s2)
    {
        try
        {
            File file = new File(Main.getInst().getDataFolder(), s2+".txt");
            if(!file.exists())
            {
                file.createNewFile();
            }
            
            FileWriter fileWritter = new FileWriter(file, true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(s);
            bufferWritter.newLine();
            bufferWritter.close();
        }
        catch (Exception e)
        {
            Main.getInst().getLogger().warning(s2+":"+s);
        }
    }
    
    public static void newLog(Plugin main, String s, String s2)
    {
        try
        {
            File file = new File(main.getDataFolder(), s2+".txt");
            if(!file.exists())
            {
                file.createNewFile();
            }
            
            FileWriter fileWritter = new FileWriter(file, true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write("["+DateUtils.CH_ALL.format(System.currentTimeMillis())+"]"+s);
            bufferWritter.newLine();
            bufferWritter.close();
        }
        catch (Exception e)
        {
            Main.getInst().getLogger().warning(s2+":"+s);
        }
    }

}

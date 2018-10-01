package me.skymc.taboolib.commands.internal.plugin;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.commands.internal.type.CommandArgument;

import java.util.stream.IntStream;

/**
 * @Author sky
 * @Since 2018-09-30 19:26
 */
public class TLibLocale {

    public static String title(String name) {
        return TLocale.asString("COMMANDS." + name + ".COMMAND-TITLE");
    }

    public static String description(String name, String label) {
        return TLocale.asString("COMMANDS." + name + "." + label + ".DESCRIPTION");
    }

    public static CommandArgument[] arguments(String name, String label, int size) {
        return IntStream.range(0, size).mapToObj(i -> new CommandArgument(TLocale.asString("COMMANDS." + name + "." + label + ".ARGUMENTS." + i))).toArray(CommandArgument[]::new);
    }

    public static CommandArgument argument(String name, String label, int index) {
        return new CommandArgument(TLocale.asString("COMMANDS." + name + "." + label + ".ARGUMENTS." + index));
    }
}

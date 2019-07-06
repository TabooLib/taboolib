package io.izzel.taboolib.module.command.lite;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * 为了防止与 TabExecutor 混淆所以名称改为 CompleterTab
 *
 * @author sky
 */
public interface CompleterTab {

    List<String> execute(CommandSender sender, String[] args);

}
package io.izzel.taboolib.module.command.lite;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 为了防止与 TabExecutor 混淆所以名称改为 CompleterTab
 *
 * @author sky
 */
public interface CompleterTab {

    @Nullable List<String> execute(@NotNull CommandSender sender, @NotNull String[] args);

}
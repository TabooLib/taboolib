package me.skymc.taboolib.commands.internal.type;

import java.util.List;

/**
 * 为了防止与 TabExecutor 混淆所以名称改为 CompleterTab
 *
 * @author sky
 */
public interface CommandTab {

    List<String> run();

}
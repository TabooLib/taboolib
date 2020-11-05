package io.izzel.taboolib.module.command.base;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 为了防止与 TabExecutor 混淆所以名称改为 CompleterTab
 *
 * @author sky
 */
public interface CommandTab {

    @Nullable List<String> run();

}
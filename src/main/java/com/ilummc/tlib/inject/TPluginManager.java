package com.ilummc.tlib.inject;

import java.io.File;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;

import me.skymc.taboolib.Main;

public class TPluginManager implements PluginManager {

    private final PluginManager instance;

    private final Main main = (Main) Main.getInst();

    public TPluginManager() {
        instance = Bukkit.getPluginManager();
    }

    @Override
    public void registerInterface(Class<? extends PluginLoader> aClass) throws IllegalArgumentException {
        instance.registerInterface(aClass);
    }

    @Override
    public Plugin getPlugin(String s) {
        return instance.getPlugin(s);
    }

    @Override
    public Plugin[] getPlugins() {
        return instance.getPlugins();
    }

    @Override
    public boolean isPluginEnabled(String s) {
        return instance.isPluginEnabled(s);
    }

    @Override
    public boolean isPluginEnabled(Plugin plugin) {
        return instance.isPluginEnabled(plugin);
    }

    @Override
    public Plugin loadPlugin(File file) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        return instance.loadPlugin(file);
    }

    @Override
    public Plugin[] loadPlugins(File file) {
        return instance.loadPlugins(file);
    }

    @Override
    public void disablePlugins() {
        for (Plugin plugin : getPlugins()) {
            if (plugin != main) disablePlugin(plugin);
        }
        disablePlugin(main);
    }

    @Override
    public void clearPlugins() {
        instance.clearPlugins();
    }

    @Override
    public void callEvent(Event event) throws IllegalStateException {
        instance.callEvent(event);
    }

    @Override
    public void registerEvents(Listener listener, Plugin plugin) {
        instance.registerEvents(listener, plugin);
    }

    @Override
    public void registerEvent(Class<? extends Event> aClass, Listener listener, EventPriority eventPriority, EventExecutor eventExecutor, Plugin plugin) {
        instance.registerEvent(aClass, listener, eventPriority, eventExecutor, plugin);
    }

    @Override
    public void registerEvent(Class<? extends Event> aClass, Listener listener, EventPriority eventPriority, EventExecutor eventExecutor, Plugin plugin, boolean b) {
        instance.registerEvent(aClass, listener, eventPriority, eventExecutor, plugin, b);
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        TDependencyInjector.injectOnEnable(plugin);
        instance.enablePlugin(plugin);
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        TDependencyInjector.onDisable(plugin);
        instance.disablePlugin(plugin);
    }

    @Override
    public Permission getPermission(String s) {
        return instance.getPermission(s);
    }

    @Override
    public void addPermission(Permission permission) {
        instance.addPermission(permission);
    }

    @Override
    public void removePermission(Permission permission) {
        instance.removePermission(permission);
    }

    @Override
    public void removePermission(String s) {
        instance.removePermission(s);
    }

    @Override
    public Set<Permission> getDefaultPermissions(boolean b) {
        return instance.getDefaultPermissions(b);
    }

    @Override
    public void recalculatePermissionDefaults(Permission permission) {
        instance.recalculatePermissionDefaults(permission);
    }

    @Override
    public void subscribeToPermission(String s, Permissible permissible) {
        instance.subscribeToPermission(s, permissible);
    }

    @Override
    public void unsubscribeFromPermission(String s, Permissible permissible) {
        instance.unsubscribeFromPermission(s, permissible);
    }

    @Override
    public Set<Permissible> getPermissionSubscriptions(String s) {
        return instance.getPermissionSubscriptions(s);
    }

    @Override
    public void subscribeToDefaultPerms(boolean b, Permissible permissible) {
        instance.subscribeToDefaultPerms(b, permissible);
    }

    @Override
    public void unsubscribeFromDefaultPerms(boolean b, Permissible permissible) {
        instance.unsubscribeFromDefaultPerms(b, permissible);
    }

    @Override
    public Set<Permissible> getDefaultPermSubscriptions(boolean b) {
        return instance.getDefaultPermSubscriptions(b);
    }

    @Override
    public Set<Permission> getPermissions() {
        return instance.getPermissions();
    }

    @Override
    public boolean useTimings() {
        return instance.useTimings();
    }
}

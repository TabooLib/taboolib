package me.skymc.taboolib.particle;

import org.bukkit.*;
import org.bukkit.Color;

public class ParticleUtils {

    public static void sendColor(Effect particle, Location l, int data, Color color) {
        l.getWorld().spigot().playEffect(l, particle, data, 0, (float)getColor(color.getRed()), (float)getColor(color.getGreen()), (float)getColor(color.getBlue()), 1, 0, 35);
    }

    public static void sendColor(Effect particle, Location l, int data, java.awt.Color color) {
        l.getWorld().spigot().playEffect(l, particle, data, 0, (float)getColor(color.getRed()), (float)getColor(color.getGreen()), (float)getColor(color.getBlue()), 1, 0, 35);
    }

    public static void sendEffect(Effect particle, Location l, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        l.getWorld().spigot().playEffect(l, particle, 0, 0, offsetX, offsetY, offsetZ, speed, amount, 35);
    }

    private static double getColor(double value) {
        if (value <= 0) {
            value = -1;
        }
        return value / 255;
    }
}
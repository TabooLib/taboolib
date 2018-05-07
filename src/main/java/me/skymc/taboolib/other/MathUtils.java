package me.skymc.taboolib.other;

import me.skymc.taboolib.particle.EffLib;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MathUtils {

    public static double tronc(double paramDouble, int paramInt) {
        double d = Math.pow(10.0D, paramInt);
        return Math.floor(paramDouble * d) / d;
    }

    public static void drawVec(Vector paramVector, Location paramLocation, Color paramColor) {
        for (double d = 0.0D; d < 1.0D; d += 0.1D) {
            EffLib.REDSTONE.display(new EffLib.OrdinaryColor(paramColor), paramLocation.clone().add(paramVector.clone().multiply(d)), 100.0D);
        }
    }

    public static void sendCoords(Player paramPlayer, Vector paramVector, String paramString) {
        paramPlayer.sendMessage(paramString + tronc(paramVector.getX(), 2) + " " + tronc(paramVector.getY(), 2) + " " + tronc(paramVector.getZ(), 2));
    }

    public static void sendCoords(Player paramPlayer, Location paramLocation, String paramString) {
        paramPlayer.sendMessage(paramString + tronc(paramLocation.getX(), 2) + " " + tronc(paramLocation.getY(), 2) + " " + tronc(paramLocation.getZ(), 2));
    }

    public static Vector rotAxisX(Vector paramVector, double paramDouble) {
        double d1 = paramVector.getY() * Math.cos(paramDouble) - paramVector.getZ() * Math.sin(paramDouble);
        double d2 = paramVector.getY() * Math.sin(paramDouble) + paramVector.getZ() * Math.cos(paramDouble);
        return paramVector.setY(d1).setZ(d2);
    }

    public static Vector rotAxisY(Vector paramVector, double paramDouble) {
        double d1 = paramVector.getX() * Math.cos(paramDouble) + paramVector.getZ() * Math.sin(paramDouble);
        double d2 = paramVector.getX() * -Math.sin(paramDouble) + paramVector.getZ() * Math.cos(paramDouble);
        return paramVector.setX(d1).setZ(d2);
    }

    public static Vector rotAxisZ(Vector paramVector, double paramDouble) {
        double d1 = paramVector.getX() * Math.cos(paramDouble) - paramVector.getY() * Math.sin(paramDouble);
        double d2 = paramVector.getX() * Math.sin(paramDouble) + paramVector.getY() * Math.cos(paramDouble);
        return paramVector.setX(d1).setY(d2);
    }

    public static Vector rotateFunc(Vector paramVector, Location paramLocation) {
        double d1 = paramLocation.getYaw() / 180.0F * 3.141592653589793D;
        double d2 = paramLocation.getPitch() / 180.0F * 3.141592653589793D;
        paramVector = rotAxisX(paramVector, d2);
        paramVector = rotAxisY(paramVector, -d1);
        return paramVector;
    }
}

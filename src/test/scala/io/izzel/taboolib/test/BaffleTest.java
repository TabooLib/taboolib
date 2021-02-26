package io.izzel.taboolib.test;

import io.izzel.taboolib.util.Baffle;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class BaffleTest {

    public static void main(String[] args, Player player) {
        Baffle baffle0 = Baffle.of(10);
        Baffle baffle1 = Baffle.of(10, TimeUnit.SECONDS);
        while (baffle1.hasNext(player.getName())) {
            // ..
        }
    }
}

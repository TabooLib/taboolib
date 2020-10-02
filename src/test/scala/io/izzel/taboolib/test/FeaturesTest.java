package io.izzel.taboolib.test;

import io.izzel.taboolib.util.Features;
import org.bukkit.entity.Player;

public class FeaturesTest {

    public static void main(String[] args, Player player) {
        Features.compileScript("function main() { } main()");

        Features.dispatchCommand("say 123");

        Features.displayScoreboard(player, "", "");

        Features.inputSign(player, new String[]{ " " }, r -> {
            // ..
        });

        Features.inputChat(player, new Features.ChatInput() {

            @Override
            public void head() {
                // ..
            }

            @Override
            public boolean onChat(String message) {
                return true;
            }
        });
    }
}

package io.izzel.taboolib.locale.type;

import com.google.common.collect.Maps;
import io.izzel.taboolib.locale.TLocaleSerialize;
import io.izzel.taboolib.origin.lite.Sounds;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author sky
 * @Since 2018-05-06 14:35
 */
@Immutable
@SerializableAs("ACTION")
public class TLocaleSound extends TLocaleSerialize {

    private final List<Sounds> soundPacks;

    public TLocaleSound(List<Sounds> soundPacks) {
        this.soundPacks = soundPacks;
    }

    public static TLocaleSound valueOf(Map<String, Object> map) {
        List<Sounds> soundPacks = new ArrayList<>();
        Object sounds = map.containsKey("sounds") ? map.get("sounds") : map.getOrDefault("sound", "");
        if (sounds instanceof List) {
            soundPacks = ((List<String>) sounds).stream().map(Sounds::new).collect(Collectors.toList());
        } else {
            soundPacks.add(new Sounds(sounds.toString()));
        }
        return new TLocaleSound(soundPacks);
    }

    @Override
    public void sendTo(CommandSender sender, String... args) {
        if (sender instanceof Player) {
            soundPacks.forEach(x -> x.play((Player) sender));
        }
    }

    @Override
    public String asString(String... args) {
        return toString();
    }

    @Override
    public String toString() {
        return "soundPacks=" + "TLocaleSound{" + soundPacks + '}';
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = Maps.newHashMap();
        if (soundPacks.size() == 1) {
            map.put("sounds", soundPacks.get(0).toString());
        } else if (soundPacks.size() > 1) {
            map.put("sounds", soundPacks.stream().map(Sounds::toString).collect(Collectors.toList()));
        }
        return map;
    }
}

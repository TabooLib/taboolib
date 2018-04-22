package me.skymc.taboolib.bookformatter;

import static org.bukkit.Achievement.ACQUIRE_IRON;
import static org.bukkit.Achievement.BAKE_CAKE;
import static org.bukkit.Achievement.BOOKCASE;
import static org.bukkit.Achievement.BREED_COW;
import static org.bukkit.Achievement.BREW_POTION;
import static org.bukkit.Achievement.BUILD_BETTER_PICKAXE;
import static org.bukkit.Achievement.BUILD_FURNACE;
import static org.bukkit.Achievement.BUILD_HOE;
import static org.bukkit.Achievement.BUILD_PICKAXE;
import static org.bukkit.Achievement.BUILD_SWORD;
import static org.bukkit.Achievement.BUILD_WORKBENCH;
import static org.bukkit.Achievement.COOK_FISH;
import static org.bukkit.Achievement.DIAMONDS_TO_YOU;
import static org.bukkit.Achievement.ENCHANTMENTS;
import static org.bukkit.Achievement.END_PORTAL;
import static org.bukkit.Achievement.EXPLORE_ALL_BIOMES;
import static org.bukkit.Achievement.FLY_PIG;
import static org.bukkit.Achievement.FULL_BEACON;
import static org.bukkit.Achievement.GET_BLAZE_ROD;
import static org.bukkit.Achievement.GET_DIAMONDS;
import static org.bukkit.Achievement.GHAST_RETURN;
import static org.bukkit.Achievement.KILL_COW;
import static org.bukkit.Achievement.KILL_ENEMY;
import static org.bukkit.Achievement.KILL_WITHER;
import static org.bukkit.Achievement.MAKE_BREAD;
import static org.bukkit.Achievement.MINE_WOOD;
import static org.bukkit.Achievement.NETHER_PORTAL;
import static org.bukkit.Achievement.ON_A_RAIL;
import static org.bukkit.Achievement.OPEN_INVENTORY;
import static org.bukkit.Achievement.OVERKILL;
import static org.bukkit.Achievement.OVERPOWERED;
import static org.bukkit.Achievement.SNIPE_SKELETON;
import static org.bukkit.Achievement.SPAWN_WITHER;
import static org.bukkit.Achievement.THE_END;

import java.util.HashMap;

import org.bukkit.Achievement;

public final class BookAchievement {
	
	private static final HashMap<Achievement, String> achievements = new HashMap<>();
	
	static {
		achievements.put(OPEN_INVENTORY, "openInventory");
		achievements.put(MINE_WOOD, "mineWood");
		achievements.put(BUILD_WORKBENCH, "buildWorkBench");
		achievements.put(BUILD_PICKAXE, "buildPickaxe");
		achievements.put(BUILD_FURNACE, "buildFurnace");
		achievements.put(ACQUIRE_IRON, "aquireIron");
		achievements.put(BUILD_HOE, "buildHoe");
		achievements.put(MAKE_BREAD, "makeBread");
		achievements.put(BAKE_CAKE,"bakeCake");
		achievements.put(BUILD_BETTER_PICKAXE,"buildBetterPickaxe");
		achievements.put(COOK_FISH,"cookFish");
		achievements.put(ON_A_RAIL,"onARail");
		achievements.put(BUILD_SWORD,"buildSword");
		achievements.put(KILL_ENEMY,"killEnemy");
		achievements.put(KILL_COW,"killCow");
		achievements.put(FLY_PIG,"flyPig");
		achievements.put(SNIPE_SKELETON,"snipeSkeleton");
		achievements.put(GET_DIAMONDS,"diamonds");
		achievements.put(NETHER_PORTAL,"portal");
		achievements.put(GHAST_RETURN,"ghast");
		achievements.put(GET_BLAZE_ROD,"blazerod");
		achievements.put(BREW_POTION,"potion");
        achievements.put(END_PORTAL,"thEnd");
        achievements.put(THE_END,"theEnd2");
        achievements.put(ENCHANTMENTS,"enchantments");
        achievements.put(OVERKILL,"overkill");
        achievements.put(BOOKCASE,"bookacase");
        achievements.put(EXPLORE_ALL_BIOMES,"exploreAllBiomes");
        achievements.put(SPAWN_WITHER,"spawnWither");
        achievements.put(KILL_WITHER,"killWither");
        achievements.put(FULL_BEACON,"fullBeacon");
        achievements.put(BREED_COW,"breedCow");
        achievements.put(DIAMONDS_TO_YOU,"diamondsToYou");
        achievements.put(OVERPOWERED, "overpowered");
	}

    /**
     * Gets the json id from the bukkit achievement passed as argument
     * @param achievement the achievement
     * @return the achievement's id or null if not found
     */
    public static String toId(Achievement achievement) {
        return achievements.get(achievement);
    }

    private BookAchievement(){
    	
    }
}

package mc.rpgstats.advancemnents;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class AdvancementHelper {
    final static Identifier LEVEL_1 = new Identifier(RPGStats.MOD_ID, "levels_1");
    final static Identifier LEVEL_5 = new Identifier(RPGStats.MOD_ID, "levels_5");
    final static Identifier LEVEL_13 = new Identifier(RPGStats.MOD_ID, "levels_13");
    final static Identifier LEVEL_25 = new Identifier(RPGStats.MOD_ID, "levels_25");
    final static Identifier LEVEL_50 = new Identifier(RPGStats.MOD_ID, "levels_50");
    final static Identifier LEVELS_MAX = new Identifier(RPGStats.MOD_ID, "levels_max");
    
    public static boolean shouldGrant(Identifier id, ServerPlayerEntity playerEntity) {
        if (id.equals(LEVEL_1)) {
            return RPGStats.getHighestLevel(ComponentProvider.fromEntity(playerEntity)) >= 1;
        }
        if (id.equals(LEVEL_5)) {
            return RPGStats.getHighestLevel(ComponentProvider.fromEntity(playerEntity)) >= 5;
        }
        if (id.equals(LEVEL_13)) {
            return RPGStats.getHighestLevel(ComponentProvider.fromEntity(playerEntity)) >= 13;
        }
        if (id.equals(LEVEL_25)) {
            return RPGStats.getHighestLevel(ComponentProvider.fromEntity(playerEntity)) >= 25;
        }
        if (id.equals(LEVEL_50)) {
            return RPGStats.getHighestLevel(ComponentProvider.fromEntity(playerEntity)) >= 50;
        }
        if (id.equals(LEVELS_MAX)) {
            return RPGStats.getLowestLevel(ComponentProvider.fromEntity(playerEntity)) >= 50;
        }
        
        return false;
    }
}

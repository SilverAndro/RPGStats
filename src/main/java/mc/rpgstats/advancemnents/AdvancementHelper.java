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
    
    final static Identifier MAGIC_10 = new Identifier(RPGStats.MOD_ID, "magic_10");
    final static Identifier MAGIC_25 = new Identifier(RPGStats.MOD_ID, "magic_25");
    final static Identifier MAGIC_50 = new Identifier(RPGStats.MOD_ID, "magic_50");
    
    final static Identifier MINING_10 = new Identifier(RPGStats.MOD_ID, "mining_10");
    final static Identifier MINING_25 = new Identifier(RPGStats.MOD_ID, "mining_25");
    final static Identifier MINING_50 = new Identifier(RPGStats.MOD_ID, "mining_50");
    
    final static Identifier RANGED_10 = new Identifier(RPGStats.MOD_ID, "ranged_10");
    final static Identifier RANGED_25 = new Identifier(RPGStats.MOD_ID, "ranged_25");
    final static Identifier RANGED_50 = new Identifier(RPGStats.MOD_ID, "ranged_50");
    
    final static Identifier FARMING_10 = new Identifier(RPGStats.MOD_ID, "farming_10");
    final static Identifier FARMING_25 = new Identifier(RPGStats.MOD_ID, "farming_25");
    final static Identifier FARMING_50 = new Identifier(RPGStats.MOD_ID, "farming_50");
    
    final static Identifier DEFENSE_10 = new Identifier(RPGStats.MOD_ID, "defense_10");
    final static Identifier DEFENSE_25 = new Identifier(RPGStats.MOD_ID, "defense_25");
    final static Identifier DEFENSE_50 = new Identifier(RPGStats.MOD_ID, "defense_50");
    
    final static Identifier COMBAT_10 = new Identifier(RPGStats.MOD_ID, "combat_10");
    final static Identifier COMBAT_25 = new Identifier(RPGStats.MOD_ID, "combat_25");
    final static Identifier COMBAT_50 = new Identifier(RPGStats.MOD_ID, "combat_50");
    
    final static Identifier FISHING_10 = new Identifier(RPGStats.MOD_ID, "fishing_10");
    final static Identifier FISHING_25 = new Identifier(RPGStats.MOD_ID, "fishing_25");
    final static Identifier FISHING_50 = new Identifier(RPGStats.MOD_ID, "fishing_50");
    
    public static boolean shouldGrant(Identifier id, ServerPlayerEntity playerEntity) {
        if (id.getPath().startsWith("levels_")) {
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
        }
    
        if (id.getPath().startsWith("magic_")) {
            if (id.equals(MAGIC_10)) {
                return RPGStats.getComponentLevel(RPGStats.MAGIC_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 10;
            }
            if (id.equals(MAGIC_25)) {
                return RPGStats.getComponentLevel(RPGStats.MAGIC_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 25;
            }
            if (id.equals(MAGIC_50)) {
                return RPGStats.getComponentLevel(RPGStats.MAGIC_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 50;
            }
        }
    
        if (id.getPath().startsWith("mining_")) {
            if (id.equals(MINING_10)) {
                return RPGStats.getComponentLevel(RPGStats.MINING_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 10;
            }
            if (id.equals(MINING_25)) {
                return RPGStats.getComponentLevel(RPGStats.MINING_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 25;
            }
            if (id.equals(MINING_50)) {
                return RPGStats.getComponentLevel(RPGStats.MINING_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 50;
            }
        }
    
        if (id.getPath().startsWith("ranged_")) {
            if (id.equals(RANGED_10)) {
                return RPGStats.getComponentLevel(RPGStats.RANGED_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 10;
            }
            if (id.equals(RANGED_25)) {
                return RPGStats.getComponentLevel(RPGStats.RANGED_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 25;
            }
            if (id.equals(RANGED_50)) {
                return RPGStats.getComponentLevel(RPGStats.RANGED_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 50;
            }
        }
    
        if (id.getPath().startsWith("farming_")) {
            if (id.equals(FARMING_10)) {
                return RPGStats.getComponentLevel(RPGStats.FARMING_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 10;
            }
            if (id.equals(FARMING_25)) {
                return RPGStats.getComponentLevel(RPGStats.FARMING_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 25;
            }
            if (id.equals(FARMING_50)) {
                return RPGStats.getComponentLevel(RPGStats.FARMING_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 50;
            }
        }
    
        if (id.getPath().startsWith("defense_")) {
            if (id.equals(DEFENSE_10)) {
                return RPGStats.getComponentLevel(RPGStats.DEFENSE_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 10;
            }
            if (id.equals(DEFENSE_25)) {
                return RPGStats.getComponentLevel(RPGStats.DEFENSE_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 25;
            }
            if (id.equals(DEFENSE_50)) {
                return RPGStats.getComponentLevel(RPGStats.DEFENSE_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 50;
            }
        }
    
        if (id.getPath().startsWith("combat_")) {
            if (id.equals(COMBAT_10)) {
                return RPGStats.getComponentLevel(RPGStats.MELEE_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 10;
            }
            if (id.equals(COMBAT_25)) {
                return RPGStats.getComponentLevel(RPGStats.MELEE_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 25;
            }
            if (id.equals(COMBAT_50)) {
                return RPGStats.getComponentLevel(RPGStats.MELEE_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 50;
            }
        }
    
        if (id.getPath().startsWith("fishing_")) {
            if (id.equals(FISHING_10)) {
                return RPGStats.getComponentLevel(RPGStats.FISHING_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 10;
            }
            if (id.equals(FISHING_25)) {
                return RPGStats.getComponentLevel(RPGStats.FISHING_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 25;
            }
            if (id.equals(FISHING_50)) {
                return RPGStats.getComponentLevel(RPGStats.FISHING_COMPONENT, ComponentProvider.fromEntity(playerEntity)) >= 50;
            }
        }
        
        return false;
    }
}

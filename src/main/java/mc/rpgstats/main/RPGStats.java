package mc.rpgstats.main;

import mc.rpgstats.advancemnents.LevelUpCriterion;
import mc.rpgstats.event.LevelUpCallback;
import mc.rpgstats.mixin.accessor.CriteriaAccessor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;

public class RPGStats implements ModInitializer {
    public static final String MOD_ID = "rpgstats";
    public static final Identifier SYNC_STATS_PACKET_ID = new Identifier(MOD_ID, "sync_stats");
    public static final Identifier SYNC_NAMES_PACKET_ID = new Identifier(MOD_ID, "sync_names");
    public static final Identifier OPEN_GUI = new Identifier(MOD_ID, "open_gui");
    
    final static Identifier LEVELS_MAX = new Identifier(RPGStats.MOD_ID, "levels_max");
    
    public static ArrayList<ServerPlayerEntity> needsStatFix = new ArrayList<>();
    
    public static LevelUpCriterion levelUpCriterion = new LevelUpCriterion();
    
    private static RPGStatsConfig configUnsafe;
    
    // Helper methods for components
    public static void setComponentXP(Identifier id, ServerPlayerEntity player, int newValue) {
        if (CustomComponents.components.containsKey(id)) {
            CustomComponents.STATS.get(player).getOrCreateID(id).xp = newValue;
        }
    }
    
    public static int getComponentXP(Identifier id, ServerPlayerEntity player) {
        if (CustomComponents.components.containsKey(id)) {
            return CustomComponents.STATS.get(player).getOrCreateID(id).xp;
        } else {
            return -1;
        }
    }
    
    public static void setComponentLevel(Identifier id, ServerPlayerEntity player, int newValue) {
        if (CustomComponents.components.containsKey(id)) {
            CustomComponents.STATS.get(player).getOrCreateID(id).level = newValue;
        }
    }
    
    public static int getComponentLevel(Identifier id, ServerPlayerEntity player) {
        if (CustomComponents.components.containsKey(id)) {
            return CustomComponents.STATS.get(player).getOrCreateID(id).level;
        } else {
            return -1;
        }
    }
    
    public static int calculateXpNeededToReachLevel(int level) {
        RPGStatsConfig config = getConfig();
        if (config.scaling.isCumulative) {
            int required = 0;
            for (int i = 1; i <= level; i++) {
                required += (int)Math.floor(Math.pow(i, config.scaling.power) * config.scaling.scale) + config.scaling.base;
            }
            return required;
        } else {
            return (int)Math.floor(Math.pow(level, config.scaling.power) * config.scaling.scale) + config.scaling.base;
        }
    }
    
    public static void addXpAndLevelUp(Identifier id, ServerPlayerEntity player, int addedXP) {
        if (CustomComponents.components.containsKey(id)) {
            int nextXP = getComponentXP(id, player) + addedXP;
            int currentLevel = getComponentLevel(id, player);
    
            if (currentLevel < getConfig().scaling.maxLevel) {
                // Enough to level up
                int nextXPForLevelUp = calculateXpNeededToReachLevel(currentLevel + 1);
                while (nextXP >= nextXPForLevelUp && currentLevel < getConfig().scaling.maxLevel) {
                    nextXP -= nextXPForLevelUp;
                    currentLevel += 1;
            
                    setComponentLevel(id, player, currentLevel);
                    player.sendMessage(new LiteralText("§aRPGStats >§r You gained a §6" +
                        CustomComponents.components.get(id) +
                        "§r level! You are now level §6" +
                        getComponentLevel(id, player)
                    ), false);
            
                    LevelUpCallback.EVENT.invoker().onLevelUp(player, id, currentLevel, false);
            
                    nextXPForLevelUp = calculateXpNeededToReachLevel(currentLevel + 1);
                }
                setComponentXP(id, player, nextXP);
            }
        }
    }
    
    public static String getFormattedLevelData(Identifier id, ServerPlayerEntity player) {
        int currentLevel = getComponentLevel(id, player);
        int xp = getComponentXP(id, player);
    
        String name = CustomComponents.components.get(id);
        String capped = name.substring(0, 1).toUpperCase() + name.substring(1);
        if (currentLevel < getConfig().scaling.maxLevel) {
            int nextXP = calculateXpNeededToReachLevel(currentLevel + 1);
            return "§6" + capped + "§r - Level: " + currentLevel + " XP: " + xp + "/" + nextXP;
        } else {
            return "§6" + capped + "§r - Level: " + currentLevel;
        }
    }
    
    public static String getNotFormattedLevelData(Identifier id, ServerPlayerEntity player) {
        int currentLevel = getComponentLevel(id, player);
        int xp = getComponentXP(id, player);
        
        String name = CustomComponents.components.get(id);
        String capped = name.substring(0, 1).toUpperCase() + name.substring(1);
        if (currentLevel < 50) {
            int nextXP = calculateXpNeededToReachLevel(currentLevel + 1);
            return capped + " - Level: " + currentLevel + " XP: " + xp + "/" + nextXP;
        } else {
            return capped + " - Level: " + currentLevel;
        }
    }
    
    public static ArrayList<Integer> getStatLevelsForPlayer(ServerPlayerEntity player) {
        ArrayList<Integer> result = new ArrayList<>();
        for (Identifier stat : CustomComponents.components.keySet()) {
            result.add(getComponentLevel(stat, player));
        }
        return result;
    }
    
    public static int getHighestLevel(ServerPlayerEntity player) {
        return Collections.max(getStatLevelsForPlayer(player));
    }
    
    public static int getLowestLevel(ServerPlayerEntity player) {
        return Collections.min(getStatLevelsForPlayer(player));
    }
    
    public static void softLevelUp(Identifier id, ServerPlayerEntity player) {
        int savedLevel = getComponentLevel(id, player);
        if (savedLevel > 50) {
            setComponentLevel(id, player, 50);
            setComponentXP(id, player, 0);
            savedLevel = 50;
        }
        for (int i = 1; i <= savedLevel; i++) {
            setComponentLevel(id, player, i);
            LevelUpCallback.EVENT.invoker().onLevelUp(player, id, i, true);
        }
    }
    
    public static RPGStatsConfig getConfig() {
        if (configUnsafe == null) {
            configUnsafe = AutoConfig.getConfigHolder(RPGStatsConfig.class).getConfig();
        }
        return configUnsafe;
    }
    
    @Override
    public void onInitialize() {
        // Criterion
        assert CriteriaAccessor.getValues() != null;
        CriteriaAccessor.getValues().put(LevelUpCriterion.ID, levelUpCriterion);
        
        // Config
        AutoConfig.register(RPGStatsConfig.class, JanksonConfigSerializer::new);
        
        // Events
        Events.registerCommandRegisters();
        Events.registerResourceReloadListeners();
        Events.registerServerTickEvents();
        Events.registerLevelUpEvents();
        Events.registerBlockBreakListeners();
        
        System.out.println("RPGStats loaded!");
    }
}

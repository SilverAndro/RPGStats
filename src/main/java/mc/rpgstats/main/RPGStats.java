package mc.rpgstats.main;

import mc.rpgstats.advancemnents.LevelUpCriterion;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;

public class RPGStats implements ModInitializer {
    public static ArrayList<ServerPlayerEntity> needsStatFix = new ArrayList<>();
    
    public static LevelUpCriterion levelUpCriterion = new LevelUpCriterion();
    
    private static RPGStatsConfig configUnsafe;
    
    // Helper methods for components

    
    public static RPGStatsConfig getConfig() {
        if (configUnsafe == null) {
            configUnsafe = AutoConfig.getConfigHolder(RPGStatsConfig.class).getConfig();
        }
        return configUnsafe;
    }
    
    @Override
    public void onInitialize() {

    }
}

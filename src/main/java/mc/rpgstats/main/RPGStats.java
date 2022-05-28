package mc.rpgstats.main;

import mc.rpgstats.advancemnents.LevelUpCriterion;
import mc.rpgstats.event.LevelUpCallback;
import mc.rpgstats.mixin.accessor.CriteriaAccessor;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class RPGStats implements ModInitializer {
    public static final String MOD_ID = "rpgstats";
    public static final Identifier SYNC_STATS_PACKET_ID = new Identifier(MOD_ID, "sync_stats");
    public static final Identifier SYNC_NAMES_PACKET_ID = new Identifier(MOD_ID, "sync_names");
    public static final Identifier OPEN_GUI = new Identifier(MOD_ID, "open_gui");
    
    final public static Logger debugLogger = LogManager.getLogger("RPGStats Debug");
    
    final static Identifier LEVELS_MAX = new Identifier(RPGStats.MOD_ID, "levels_max");
    
    public static ArrayList<ServerPlayerEntity> needsStatFix = new ArrayList<>();
    
    public static LevelUpCriterion levelUpCriterion = new LevelUpCriterion();
    
    private static RPGStatsConfig configUnsafe;
    
    static void verifyOptionalDeps() throws IOException {
        //noinspection ConstantConditions
        try (InputStream stream = RPGStats.class.getResource("/opt_deps_req.txt").openStream()) {
            final char[] buffer = new char[8192];
            final StringBuilder result = new StringBuilder();
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            int charsRead;
            while ((charsRead = reader.read(buffer, 0, buffer.length)) > 0) {
                result.append(buffer, 0, charsRead);
            }
            
            String[] text = result.toString().split("\n");
            Arrays.stream(text).iterator().forEachRemaining(s -> {
                String[] line = s.split(" ");
                String modID = line[0].trim();
                String version = line[1].trim();
                
                FabricLoader.getInstance().getModContainer(modID).ifPresent(modContainer -> {
                    Version testVersion = null;
                    try {
                        testVersion = Version.parse(version);
                    } catch (VersionParsingException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
    
                    if (modContainer.getMetadata().getVersion().compareTo(testVersion) <= -1) {
                        System.err.println("RPGStats requires " + modID + " to be at least version " + testVersion.getFriendlyString() + " but got " + modContainer.getMetadata().getVersion().getFriendlyString() + "!");
                        System.exit(-1);
                    }
                });
            });
        }
    }
    
    // Helper methods for components
    public static void setComponentXP(Identifier id, ServerPlayerEntity player, int newValue) {
        if (getConfig().debug.logRawOps) {
            debugLogger.info(player.getEntityName() + " xp was set to " + newValue + " in stat " + id.toString());
            debugLogger.info("Stat is loaded: " + CustomComponents.components.containsKey(id));
        }
        if (CustomComponents.components.containsKey(id)) {
            CustomComponents.STATS.get(player).getOrCreateID(id).setXp(newValue);
            CustomComponents.STATS.sync(player);
        }
    }
    
    public static int getComponentXP(Identifier id, ServerPlayerEntity player) {
        return CustomComponents.components.containsKey(id) ?
                CustomComponents.STATS.get(player).getOrCreateID(id).getXp()
                : -1;
    }
    
    public static void setComponentLevel(Identifier id, ServerPlayerEntity player, int newValue) {
        if (getConfig().debug.logRawOps) {
            debugLogger.info(player.getEntityName() + " level was set to " + newValue + " in stat " + id.toString());
            debugLogger.info("Stat is loaded: " + CustomComponents.components.containsKey(id));
        }
        if (CustomComponents.components.containsKey(id)) {
            CustomComponents.STATS.get(player).getOrCreateID(id).setLevel(newValue);
            CustomComponents.STATS.sync(player);
        }
    }
    
    public static int getComponentLevel(Identifier id, ServerPlayerEntity player) {
        return CustomComponents.components.containsKey(id) ?
                CustomComponents.STATS.get(player).getOrCreateID(id).getLevel()
                : -1;
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
        if (getConfig().debug.logXpGain) {
            debugLogger.info(player.getEntityName() + " gained " + addedXP + " xp in stat " + id.toString());
            debugLogger.info("Stat is loaded: " + CustomComponents.components.containsKey(id));
        }
        
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
                    CustomComponents.STATS.sync(player);
                    player.sendMessage(new LiteralText("§aRPGStats >§r ")
                        .formatted(Formatting.GREEN)
                        .append(new TranslatableText("rpgstats.levelup_1")
                            .formatted(Formatting.WHITE)
                            .append(new LiteralText(CustomComponents.components.get(id))
                                .formatted(Formatting.GOLD)
                                .append(new TranslatableText("rpgstats.levelup_2")
                                    .formatted(Formatting.WHITE)
                                    .append(new LiteralText(String.valueOf(getComponentLevel(id, player)))
                                        .formatted(Formatting.GOLD))))
                        ), false);
                    
                    LevelUpCallback.EVENT.invoker().onLevelUp(player, id, currentLevel, false);
                    
                    nextXPForLevelUp = calculateXpNeededToReachLevel(currentLevel + 1);
                }
                setComponentXP(id, player, nextXP);
                CustomComponents.STATS.sync(player);
            }
        }
    }
    
    public static MutableText getFormattedLevelData(Identifier id, ServerPlayerEntity player) {
        int currentLevel = getComponentLevel(id, player);
        int xp = getComponentXP(id, player);
        
        String name = CustomComponents.components.get(id);
        String capped = name.substring(0, 1).toUpperCase() + name.substring(1);
        if (currentLevel < getConfig().scaling.maxLevel) {
            int nextXP = calculateXpNeededToReachLevel(currentLevel + 1);
            return new LiteralText(capped)
                .formatted(Formatting.GOLD)
                .append(new TranslatableText("rpgstats.notmaxlevel_trunc", currentLevel, xp, nextXP).formatted(Formatting.WHITE));
        } else {
            return new LiteralText(capped)
                .formatted(Formatting.GOLD)
                .append(new TranslatableText("rpgstats.maxlevel_trunc", currentLevel).formatted(Formatting.WHITE));
        }
    }
    
    public static TranslatableText getNotFormattedLevelData(Identifier id, ServerPlayerEntity player) {
        int currentLevel = getComponentLevel(id, player);
        int xp = getComponentXP(id, player);
        
        String name = CustomComponents.components.get(id);
        String capped = name.substring(0, 1).toUpperCase() + name.substring(1);
        if (currentLevel < getConfig().scaling.maxLevel) {
            int nextXP = calculateXpNeededToReachLevel(currentLevel + 1);
            return new TranslatableText("rpgstats.notmaxlevel", capped, currentLevel, xp, nextXP);
        } else {
            return new TranslatableText("rpgstats.maxlevel", capped, currentLevel);
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
        if (savedLevel > getConfig().scaling.maxLevel) {
            setComponentLevel(id, player, getConfig().scaling.maxLevel);
            setComponentXP(id, player, 0);
            savedLevel = getConfig().scaling.maxLevel;
        }
        for (int i = 1; i <= savedLevel; i++) {
            setComponentLevel(id, player, i);
            LevelUpCallback.EVENT.invoker().onLevelUp(player, id, i, true);
        }
        CustomComponents.STATS.sync(player);
    }
    
    public static RPGStatsConfig getConfig() {
        if (configUnsafe == null) {
            configUnsafe = AutoConfig.getConfigHolder(RPGStatsConfig.class).getConfig();
        }
        return configUnsafe;
    }
    
    @Override
    public void onInitialize() {
        try {
            verifyOptionalDeps();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    
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
        if (FabricLoader.getInstance().isModLoaded("harvest_scythes")) {
            Events.registerHSCompat();
        }
        
        System.out.println("RPGStats loaded!");
    }
}

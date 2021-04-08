package mc.rpgstats.main;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import io.netty.buffer.Unpooled;
import mc.rpgstats.advancemnents.LevelUpCriterion;
import mc.rpgstats.command.CheatCommand;
import mc.rpgstats.command.StatsCommand;
import mc.rpgstats.component.IStatComponent;
import mc.rpgstats.component.LevelUps;
import mc.rpgstats.component.internal.PlayerPreferencesComponent;
import mc.rpgstats.event.LevelUpCallback;
import mc.rpgstats.mixin.accessor.CriteriaAccessor;
import mc.rpgstats.mixin_logic.OnSneakLogic;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class RPGStats implements ModInitializer {
    public static final String MOD_ID = "rpgstats";
    public static final Identifier SYNC_STATS_PACKET_ID = new Identifier(MOD_ID, "sync_stats");
    public static final Identifier SYNC_NAMES_PACKET_ID = new Identifier(MOD_ID, "sync_names");
    public static final Identifier OPEN_GUI = new Identifier(MOD_ID, "open_gui");
    
    final static Identifier LEVELS_MAX = new Identifier(RPGStats.MOD_ID, "levels_max");
    
    public static ArrayList<ServerPlayerEntity> needsStatFix = new ArrayList<>();
    
    public static LevelUpCriterion levelUpCriterion = new LevelUpCriterion();
    
    private static RPGStatsConfig configUnsafe;
    
    private int tickCount = 0;
    
    // Helper methods for components
    public static void setComponentXP(Identifier id, ServerPlayerEntity player, int newValue) {
        if (CustomComponents.customComponents.containsKey(id)) {
            CustomComponents.STATS.get(player).getOrCreateID(id).xp = newValue;
        }
    }
    
    public static int getComponentXP(Identifier id, ServerPlayerEntity player) {
        if (CustomComponents.customComponents.containsKey(id)) {
            return CustomComponents.STATS.get(player).getOrCreateID(id).xp;
        } else {
            return -1;
        }
    }
    
    public static void setComponentLevel(Identifier id, ServerPlayerEntity player, int newValue) {
        if (CustomComponents.customComponents.containsKey(id)) {
            CustomComponents.STATS.get(player).getOrCreateID(id).level = newValue;
        }
    }
    
    public static int getComponentLevel(Identifier id, ServerPlayerEntity player) {
        if (CustomComponents.customComponents.containsKey(id)) {
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
        if (CustomComponents.customComponents.containsKey(id)) {
            int nextXP = getComponentXP(id, player) + addedXP;
            int currentLevel = getComponentLevel(id, player);
    
            if (currentLevel < 50) {
                // Enough to level up
                int nextXPForLevelUp = calculateXpNeededToReachLevel(currentLevel + 1);
                while (nextXP >= nextXPForLevelUp && currentLevel < 50) {
                    nextXP -= nextXPForLevelUp;
                    currentLevel += 1;
            
                    setComponentLevel(id, player, currentLevel);
                    player.sendMessage(new LiteralText("§aRPGStats >§r You gained a §6" +
                        CustomComponents.customComponents.get(id) +
                        "§r level! You are now level §6" +
                        getComponentLevel(id, player)
                    ), false);
            
                    LevelUpCallback.EVENT.invoker().onLevelUp(player, id, currentLevel, true);
            
                    nextXPForLevelUp = calculateXpNeededToReachLevel(currentLevel + 1);
                }
                setComponentXP(id, player, nextXP);
            }
        }
    }
    
    public static String getFormattedLevelData(Identifier id, ServerPlayerEntity player) {
        int currentLevel = getComponentLevel(id, player);
        int xp = getComponentXP(id, player);
    
        String name = CustomComponents.customComponents.get(id);
        String capped = name.substring(0, 1).toUpperCase() + name.substring(1);
        if (currentLevel < 50) {
            int nextXP = calculateXpNeededToReachLevel(currentLevel + 1);
            return "§6" + capped + "§r - Level: " + currentLevel + " XP: " + xp + "/" + nextXP;
        } else {
            return "§6" + capped + "§r - Level: " + currentLevel;
        }
    }
    
    public static String getNotFormattedLevelData(Identifier id, ServerPlayerEntity player) {
        int currentLevel = getComponentLevel(id, player);
        int xp = getComponentXP(id, player);
        
        String name = CustomComponents.customComponents.get(id);
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
        for (Identifier stat : CustomComponents.customComponents.keySet()) {
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
        System.out.println("RPGStats is starting...");
        
        // Criterion
        assert CriteriaAccessor.getValues() != null;
        CriteriaAccessor.getValues().put(LevelUpCriterion.ID, levelUpCriterion);
        
        // Config
        AutoConfig.register(RPGStatsConfig.class, JanksonConfigSerializer::new);
        
        // Command
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, dedicated) -> {
                StatsCommand.register(dispatcher);
                CheatCommand.register(dispatcher);
            }
        );
        
        // Data driven stuff
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("rpgstats:stats");
            }
    
            @Override
            public void apply(ResourceManager manager) {
                CustomComponents.customComponents.clear();
                
                for(Identifier id : manager.findResources("rpgstats", path -> path.endsWith(".stat"))) {
                    try (InputStream stream = manager.getResource(id).getInputStream()) {
                        final char[] buffer = new char[8192];
                        final StringBuilder result = new StringBuilder();
                        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                            int charsRead;
                            while ((charsRead = reader.read(buffer, 0, buffer.length)) > 0) {
                                result.append(buffer, 0, charsRead);
                            }
    
                            String[] text = result.toString().split("\n");
                            handleLines(text);
                        }
                    } catch(Throwable e) {
                        RuntimeException clean = new RuntimeException("Failed to read " + id);
                        clean.addSuppressed(e);
                        throw clean;
                    }
                }
            }
        });
        
        // Syncing and advancements
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            tickCount++;
            if (tickCount >= 20) {
                Collection<Advancement> collection = server.getAdvancementLoader().getAdvancements();
                PlayerLookup.all(server).forEach(
                    (player) -> {
                        // Do sneak logic if holding sneak and opted out of spam
                        PlayerPreferencesComponent preferences = CustomComponents.PREFERENCES.get(player);
                        if (preferences.isOptedOutOfButtonSpam && player.isSneaking()) {
                            OnSneakLogic.doLogic(true, player);
                        }
                        
                        // Fix stats for respawning players
                        if (needsStatFix.contains(player) && player.isAlive()) {
                            softLevelUp(CustomComponents.DEFENSE, player);
                            softLevelUp(CustomComponents.FARMING, player);
                            softLevelUp(CustomComponents.MAGIC, player);
                            softLevelUp(CustomComponents.MELEE, player);
                            softLevelUp(CustomComponents.MINING, player);
                            softLevelUp(CustomComponents.RANGED, player);
                            softLevelUp(CustomComponents.FISHING, player);
                            needsStatFix.remove(player);
                        }
                        
                        // Grant the hidden max level advancement
                        Optional<Advancement> possible = collection
                            .stream()
                            .filter(advancement -> advancement.getId().equals(LEVELS_MAX))
                            .findFirst();
                        if (possible.isPresent()) {
                            if (!player.getAdvancementTracker().getProgress(possible.get()).isDone()) {
                                if (getLowestLevel(player) >= 50) {
                                    player.getAdvancementTracker().grantCriterion(possible.get(), "trigger");
                                }
                            }
                        }
                        
                        // Client has the mod installed
                        if (ServerPlayNetworking.canSend(player, SYNC_STATS_PACKET_ID)) {
                            int count = CustomComponents.customComponents.size();
    
                            PacketByteBuf nameData = new PacketByteBuf(Unpooled.buffer());
                            PacketByteBuf statData = new PacketByteBuf(Unpooled.buffer());
                            
                            // How many stats in packet
                            statData.writeInt(count);
                            nameData.writeInt(count);
                            // For each stat
                            for (Identifier statId : CustomComponents.customComponents.keySet()) {
                                // Write the stat identifier
                                statData.writeIdentifier(statId);
                                nameData.writeIdentifier(statId);
                                // Write the level and XP
                                statData.writeInt(getComponentLevel(statId, player));
                                statData.writeInt(getComponentXP(statId, player));
                                nameData.writeString(CustomComponents.customComponents.get(statId));
                            }
                            
                            ServerPlayNetworking.send(player, SYNC_STATS_PACKET_ID, statData);
                            ServerPlayNetworking.send(player, SYNC_NAMES_PACKET_ID, nameData);
                        }
                        
                        // Mining lv 50 effect
                        if (
                            player.getBlockPos().getY() <= getConfig().toggles.mining.effectLevelTrigger
                                && getComponentLevel(CustomComponents.MINING, player) >= 50
                                && getConfig().toggles.mining.enableLv50Buff
                        ) {
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 13 * 20));
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 2 * 20, 1));
                        }
                    }
                );
                tickCount = 0;
            }
        });
    
        LevelUps.registerLevelUpEvents();
        
        System.out.println("RPGStats is done loading");
    }
    
    private void handleLines(String[] text) {
        for (String line : text) {
            line = line.replace("\r", "");
    
            String[] split = line.split(">");
            String id = split[0];
            String name = split[1];
            
            Identifier possible;
            if (!id.startsWith("-")) {
                possible = Identifier.tryParse(id);
            } else {
                possible = Identifier.tryParse(id.substring(1));
            }
            if (possible != null) {
                CustomComponents.customComponents.put(possible, name);
            } else {
                throw new RuntimeException(line);
            }
        }
    }
}

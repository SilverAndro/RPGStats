package mc.rpgstats.main;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import io.netty.buffer.Unpooled;
import mc.rpgstats.advancemnents.AdvancementHelper;
import mc.rpgstats.command.CheatCommand;
import mc.rpgstats.command.StatsCommand;
import mc.rpgstats.component.IStatComponent;
import mc.rpgstats.event.LevelUpCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class RPGStats implements ModInitializer {
    public static final String MOD_ID = "rpgstats";
    public static final Identifier SYNC_STATS_PACKET_ID = new Identifier(MOD_ID, "sync_stats");
    public static final Identifier OPEN_GUI = new Identifier(MOD_ID, "open_gui");
    
    public static ArrayList<ServerPlayerEntity> needsStatFix = new ArrayList<>();
    
    private int tickCount = 0;
    
    @Override
    public void onInitialize() {
        System.out.println("RPGStats is starting...");
        
        // Command
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, dedicated) -> {
                StatsCommand.register(dispatcher);
                CheatCommand.register(dispatcher);
            }
        );
        
        // Syncing and advancements
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            tickCount++;
            if (tickCount >= 20) {
                Collection<Advancement> collection = server.getAdvancementLoader().getAdvancements();
                PlayerLookup.all(server).forEach(
                    (player) -> {
                        if (needsStatFix.contains(player) && player.isAlive()) {
                            softLevelUp(StatComponents.DEFENSE_COMPONENT, player);
                            softLevelUp(StatComponents.FARMING_COMPONENT, player);
                            softLevelUp(StatComponents.MAGIC_COMPONENT, player);
                            softLevelUp(StatComponents.MELEE_COMPONENT, player);
                            softLevelUp(StatComponents.MINING_COMPONENT, player);
                            softLevelUp(StatComponents.RANGED_COMPONENT, player);
                            softLevelUp(StatComponents.FISHING_COMPONENT, player);
                            needsStatFix.remove(player);
                        }
                        
                        for (Advancement advancement : collection) {
                            if (advancement.getId().getNamespace().equals("rpgstats")) {
                                if (!player.getAdvancementTracker().getProgress(advancement).isDone()) {
                                    if (AdvancementHelper.shouldGrant(advancement.getId(), player)) {
                                        player.getAdvancementTracker().grantCriterion(advancement, "trigger");
                                    }
                                }
                            }
                        }
                        
                        // Client has the mod installed
                        if (ServerPlayNetworking.canSend(player, SYNC_STATS_PACKET_ID)) {
                            int count = StatComponents.statList.size();
    
                            PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                            
                            // How many stats in packet
                            passedData.writeInt(count);
                            // For each stat
                            for (Identifier statIdent : StatComponents.idToComponentIndexMap.keySet()) {
                                // Write the stat identifier
                                passedData.writeIdentifier(statIdent);
                                // Get the actual key
                                ComponentKey<? extends IStatComponent> stat = statFromID(statIdent);
                                // Write the level and XP
                                passedData.writeInt(getComponentLevel(stat, player));
                                passedData.writeInt(getComponentXP(stat, player));
                            }
                            
                            ServerPlayNetworking.send(player, SYNC_STATS_PACKET_ID, passedData);
                        }
                        
                        if (player.getBlockPos().getY() <= 40 && getComponentLevel(StatComponents.MINING_COMPONENT, player) >= 50) {
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 13 * 20));
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 2 * 20, 1));
                        }
                    }
                );
                tickCount = 0;
            }
        });
    
        System.out.println("RPGStats is done loading");
    }
    
    // Helper methods for components
    public static void setComponentXP(ComponentKey<? extends IStatComponent> type, ServerPlayerEntity player, int newValue) {
        type.get(player).setXP(newValue);
    }
    
    public static int getComponentXP(ComponentKey<? extends IStatComponent> type, ServerPlayerEntity player) {
        return type.get(player).getXP();
    }
    
    public static void setComponentLevel(ComponentKey<? extends IStatComponent> type, ServerPlayerEntity player, int newValue) {
        type.get(player).setLevel(newValue);
    }
    
    public static int getComponentLevel(ComponentKey<? extends IStatComponent> type, ServerPlayerEntity player) {
        return type.get(player).getLevel();
    }
    
    public static int calculateXpNeededToReachLevel(int level) {
        return (int)Math.floor(Math.pow(level, 2.05) * 0.5) + 80;
    }
    
    public static void addXpAndLevelUp(ComponentKey<? extends IStatComponent> type, ServerPlayerEntity player, int addedXP) {
        int nextXP = getComponentXP(type, player) + addedXP;
        int currentLevel = getComponentLevel(type, player);
        
        if (currentLevel < 50) {
            // Enough to level up
            int nextXPForLevelUp = calculateXpNeededToReachLevel(currentLevel + 1);
            while (nextXP >= nextXPForLevelUp && currentLevel < 50) {
                nextXP -= nextXPForLevelUp;
                currentLevel += 1;
                
                setComponentLevel(type, player, currentLevel);
                ((PlayerEntity)type.get(player).getEntity()).sendMessage(new LiteralText("§aRPGStats >§r You gained a §6" + type.get(player).getName() + "§r level! You are now level §6" + type.get(player).getLevel()), false);
                type.get(player).onLevelUp(false);
                
                LevelUpCallback.EVENT.invoker().onLevelUp(player, type, currentLevel);
    
                nextXPForLevelUp = calculateXpNeededToReachLevel(currentLevel + 1);
            }
            setComponentXP(type, player, nextXP);
        }
    }
    
    public static String getFormattedLevelData(ComponentKey<? extends IStatComponent> type, ServerPlayerEntity player) {
        int currentLevel = getComponentLevel(type, player);
        int xp = getComponentXP(type, player);
        if (currentLevel < 50) {
            int nextXP = calculateXpNeededToReachLevel(currentLevel + 1);
            return "§6" + type.get(player).getCapName() + "§r - Level: " + currentLevel + " XP: " + xp + "/" + nextXP;
        } else {
            return "§6" + type.get(player).getCapName() + "§r - Level: " + currentLevel;
        }
    }
    
    public static String getNotFormattedLevelData(ComponentKey<? extends IStatComponent> type, ServerPlayerEntity player) {
        int currentLevel = getComponentLevel(type, player);
        int xp = getComponentXP(type, player);
        if (currentLevel < 50) {
            int nextXP = calculateXpNeededToReachLevel(currentLevel + 1);
            return type.get(player).getCapName() + " - Level: " + currentLevel + " XP: " + xp + "/" + nextXP;
        } else {
            return "" + type.get(player).getCapName() + " - Level: " + currentLevel;
        }
    }
    
    public static ArrayList<Integer> getStatLevelsForPlayer(ServerPlayerEntity player) {
        ArrayList<Integer> result = new ArrayList<>();
        for (ComponentKey<? extends IStatComponent> stat : StatComponents.statList) {
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
    
    public static void softLevelUp(ComponentKey<? extends IStatComponent> type, ServerPlayerEntity player) {
        int savedLevel = getComponentLevel(type, player);
        if (savedLevel > 50) {
            setComponentLevel(type, player, 50);
            setComponentXP(type, player, 0);
            savedLevel = 50;
        }
        for (int i = 1; i <= savedLevel; i++) {
            setComponentLevel(type, player, i);
            type.get(player).onLevelUp(true);
        }
    }
    
    public static ComponentKey<? extends IStatComponent> statFromID(Identifier ID) {
        return StatComponents.statList.get(StatComponents.idToComponentIndexMap.get(ID));
    }
}

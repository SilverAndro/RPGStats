package mc.rpgstats.main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import mc.rpgstats.command.CheatCommand;
import mc.rpgstats.command.StatsCommand;
import mc.rpgstats.component.internal.PlayerPreferencesComponent;
import mc.rpgstats.event.LevelUpCallback;
import mc.rpgstats.mixin_logic.OnSneakLogic;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.advancement.Advancement;
import net.minecraft.block.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import wraith.harvest_scythes.api.scythe.HSScythesEvents;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static mc.rpgstats.main.RPGStats.getConfig;
import static mc.rpgstats.main.RPGStats.softLevelUp;

@SuppressWarnings("ConstantConditions")
public class Events {
    private static int tickCount = 0;
    private static final ConcurrentHashMap<BlockPos, Integer> blacklistedPos = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Identifier, Integer> blockMiningXp = new ConcurrentHashMap<>();

    public static void registerCommandRegisters() {
        // Commands
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> {
                StatsCommand.register(dispatcher);
                CheatCommand.register(dispatcher);
            }
        );
    }
    
    public static void registerHSCompat() {
        HSScythesEvents.addHarvestListener(harvestEvent -> {
            if (harvestEvent.user() instanceof ServerPlayerEntity) {
                RPGStats.addXpAndLevelUp(CustomComponents.FARMING, (ServerPlayerEntity)harvestEvent.user(), harvestEvent.totalBlocksHarvested());
            }
        });
    }
    
    @SuppressWarnings("DuplicatedCode")
    public static void registerResourceReloadListeners() {
        // Data driven stuff
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("rpgstats:stats");
            }
        
            @Override
            public void reload(ResourceManager manager) {
                CustomComponents.components.clear();

                System.out.println("RPGStats reload!");
                try {
                    for (Map.Entry<Identifier, List<Resource>> entry : manager.findAllResources("rpgstats", identifier -> identifier.getPath().endsWith(".stat")).entrySet()) {
                        for (Resource resource : entry.getValue()) {
                            handleLines(resource.getReader().lines().toArray(String[]::new));
                        }
                    }
                } catch (IOException err) {
                    RuntimeException clean = new RuntimeException("Failed to read resources");
                    clean.addSuppressed(err);
                    throw clean;
                }
            }
        });

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("rpgstats:block_xp");
            }

            @Override
            public void reload(ResourceManager manager) {
                blockMiningXp.clear();
                try {
                    for (Map.Entry<Identifier, List<Resource>> entry : manager.findAllResources("rpgstats", identifier -> identifier.getPath().contains("mining_xp")).entrySet()) {
                        for (Resource resource : entry.getValue()) {
                            Gson gson = new Gson();
                            Type mapType = new TypeToken<Map<String, Integer>>() {}.getType();
                            Map<String, Integer> data = gson.fromJson(resource.getReader(), mapType);
                            data.forEach((identifier, integer) -> blockMiningXp.put(new Identifier(identifier), integer));
                        }
                    }
                } catch (IOException err) {
                    RuntimeException clean = new RuntimeException("Failed to read resources");
                    clean.addSuppressed(err);
                    throw clean;
                }
            }
        });
    }
    
    private static void handleLines(String[] text) {
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
                if (id.startsWith("-")) {
                    CustomComponents.components.remove(possible);
                } else {
                    CustomComponents.components.put(possible, name);
                }
            } else {
                throw new RuntimeException(line);
            }
        }
    }
    
    public static void registerServerTickEvents() {
        // Syncing and advancements
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            ArrayList<BlockPos> toRemove = new ArrayList<>();
            blacklistedPos.forEach((blockPos, integer) -> {
                blacklistedPos.put(blockPos, integer - 1);
                if (integer <= 0) {
                    toRemove.add(blockPos);
                }
            });
            toRemove.forEach(blacklistedPos::remove);
            
            tickCount++;
            if (tickCount >= 200) {
                Collection<Advancement> collection = server.getAdvancementLoader().getAdvancements();
                PlayerLookup.all(server).forEach(
                    (player) -> {
                        // Do sneak logic if holding sneak and opted out of spam
                        PlayerPreferencesComponent preferences = CustomComponents.PREFERENCES.get(player);
                        if (preferences.isOptedOutOfButtonSpam && player.isSneaking()) {
                            OnSneakLogic.doLogic(true, player);
                        }
                    
                        // Fix stats for respawning players
                        if (RPGStats.needsStatFix.contains(player) && player.isAlive()) {
                            for (Identifier id : CustomComponents.components.keySet()) {
                                softLevelUp(id, player);
                            }
                            RPGStats.needsStatFix.remove(player);
                        }
                    
                        // Grant the hidden max level advancement
                        Optional<Advancement> possible = collection
                            .stream()
                            .filter(advancement -> advancement.getId().equals(RPGStats.LEVELS_MAX))
                            .findFirst();
                        if (possible.isPresent()) {
                            if (!player.getAdvancementTracker().getProgress(possible.get()).isDone()) {
                                if (RPGStats.getLowestLevel(player) >= 50) {
                                    player.getAdvancementTracker().grantCriterion(possible.get(), "trigger");
                                }
                            }
                        }
                    
                        // Client has the mod installed
                        if (ServerPlayNetworking.canSend(player, RPGStats.SYNC_STATS_PACKET_ID)) {
                            int count = CustomComponents.components.size();
                        
                            PacketByteBuf nameData = new PacketByteBuf(Unpooled.buffer());
                            PacketByteBuf statData = new PacketByteBuf(Unpooled.buffer());
                        
                            // How many stats in packet
                            statData.writeInt(count);
                            nameData.writeInt(count);
                            // For each stat
                            for (Identifier statId : CustomComponents.components.keySet()) {
                                // Write the stat identifier
                                statData.writeIdentifier(statId);
                                nameData.writeIdentifier(statId);
                                // Write the level and XP
                                statData.writeInt(RPGStats.getComponentLevel(statId, player));
                                statData.writeInt(RPGStats.getComponentXP(statId, player));
                                nameData.writeString(CustomComponents.components.get(statId));
                            }
                        
                            ServerPlayNetworking.send(player, RPGStats.SYNC_STATS_PACKET_ID, statData);
                            ServerPlayNetworking.send(player, RPGStats.SYNC_NAMES_PACKET_ID, nameData);
                        }
                    
                        // Mining lv 50 effect
                        if (
                            player.getBlockPos().getY() <= RPGStats.getConfig().toggles.mining.effectLevelTrigger
                                && RPGStats.getComponentLevel(CustomComponents.MINING, player) >= 50
                                && RPGStats.getConfig().toggles.mining.enableLv50Buff
                        ) {
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 13 * 20, 0, true, false, true));
                        }
                        CustomComponents.STATS.sync(player);
                    }
                );
                tickCount = 0;
            }
        });
    }
    
    public static void registerLevelUpEvents() {
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.DEFENSE)) {
                player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) + 0.01);
                if (!hideMessages)
                    player.sendMessage(Text.literal("§a+0.01§r Knockback resistance"), false);
                if (newLevel % RPGStats.getConfig().defenseHP.everyXLevels == 0 && newLevel > RPGStats.getConfig().defenseHP.afterLevel) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH) + RPGStats.getConfig().defenseHP.addAmount);
                    if (!hideMessages)
                        player.sendMessage(Text.literal("§a+1§r Health"), false);
                }
    
                if (!hideMessages) {
                    if (newLevel == 25) {
                        player.sendMessage(Text.literal("§aNimble§r - 5% chance to avoid damage"), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(Text.literal("§aNimble II§r - 10% chance to avoid damage"), false);
                    }
                }
            }
        });
    
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.FARMING)) {
                if (!hideMessages) {
                    player.sendMessage(Text.literal("§a+1§r Bonemeal efficiency"), false);
        
                    if (newLevel == 25) {
                        player.sendMessage(Text.literal("§aNurturing§r - Shift rapidly to grow nearby crops (while holding hoe)"), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(Text.literal("§aNurturing II§r - Nurturing has increased range"), false);
                    }
                }
            }
        });
    
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.FISHING)) {
                player.getAttributeInstance(EntityAttributes.GENERIC_LUCK).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_LUCK) + 0.05);
    
                if (!hideMessages) {
                    player.sendMessage(Text.literal("§a+0.05§r Luck"), false);
                    if (newLevel == 25) {
                        player.sendMessage(Text.literal("§aVitamin rich§r - Eating fish grants you a temporary positive effect"), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(Text.literal("§aTeach a man to fish§r - Extra saturation when eating"), false);
                    }
                }
            }
        });
    
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.MAGIC)) {
                if (!hideMessages) {
                    player.sendMessage(Text.literal("§a+1§r Drunk potion duration"), false);
        
                    if (newLevel % 3 == 0) {
                        player.sendMessage(Text.literal("§a+1§r Potion drink speed"), false);
                    }
        
                    if (newLevel == 25) {
                        player.sendMessage(Text.literal("§aVax§r - Immune to poison"), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(Text.literal("§aDead inside§r - Immune to wither"), false);
                    }
                }
            }
        });
    
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.MELEE)) {
                player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) + getConfig().melee.attackDamagePerLevel);
    
                if (!hideMessages) {
                    player.sendMessage(Text.literal("§a+" + getConfig().melee.attackDamagePerLevel + "§r Melee damage"), false);
                    if (newLevel == 25) {
                        player.sendMessage(Text.literal("§aBloodthirst§r - Regain 1 heart after killing a monster"), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(Text.literal("§aBloodthirst II§r - Regain 2 hearts after killing a monster"), false);
                    }
                }
            }
        });
    
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.MINING)) {
                if (!hideMessages) {
                    player.sendMessage(Text.literal("§a+0.1§r Additional Mining Speed"), false);
        
                    if (newLevel == 25) {
                        player.sendMessage(Text.literal("§aMagically infused§r - Extra 5% chance to not consume durability with unbreaking."), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(Text.literal("§aMiners sight§r - Night vision below y" + RPGStats.getConfig().toggles.mining.effectLevelTrigger), false);
                    }
                }
            }
        });
    
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.RANGED)) {
                if (!hideMessages) {
                    player.sendMessage(Text.literal("§a+1§r Bow accuracy"), false);
        
                    if (newLevel == 25) {
                        player.sendMessage(Text.literal("§aAqueus§r - Impaling applies to all mobs, not just water based ones"), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(Text.literal("§aNix§r - You no longer need arrows"), false);
                    }
                }
            }
        });
    }
    
    public static void registerBlockBreakListeners() {
        PlayerBlockBreakEvents.AFTER.register((world, playerEntity, blockPos, blockState, blockEntity) -> {
            if (!world.isClient) {
                if (RPGStats.getConfig().antiCheat.blockBreakPos) {
                    if (blacklistedPos.containsKey(blockPos)) {
                        if (getConfig().debug.logAntiCheatPrevention) {
                            RPGStats.debugLogger.info("Ignoring block break at " + blockPos + " because it was previously broken");
                        }
                        return;
                    } else {
                        blacklistedPos.put(blockPos, getConfig().antiCheat.blockBreakDelay);
                    }
                }
                
                Block block = blockState.getBlock();
                if (RPGStats.getConfig().debug.logBrokenBlocks) {
                    RPGStats.debugLogger.info(playerEntity.getEntityName() + " broke " + block.getTranslationKey() + " at " + blockPos);
                }
                if (block instanceof CropBlock || block instanceof PumpkinBlock || block instanceof MelonBlock || block instanceof CocoaBlock) {
                    if (block instanceof CropBlock) {
                        if (((CropBlock)block).isMature(blockState)) {
                            RPGStats.addXpAndLevelUp(CustomComponents.FARMING, (ServerPlayerEntity)playerEntity, 1);
                        }
                    } else {
                        RPGStats.addXpAndLevelUp(CustomComponents.FARMING, (ServerPlayerEntity)playerEntity, 1);
                    }
                }
        
                Random random = new Random();
                if (random.nextBoolean()) {
                    int amount;
                    amount = blockMiningXp.getOrDefault(Registry.BLOCK.getId(block), 0);
                    RPGStats.addXpAndLevelUp(CustomComponents.MINING, (ServerPlayerEntity)playerEntity, amount);
                }
            }
        });
    }
}

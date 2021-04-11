package mc.rpgstats.main;

import io.netty.buffer.Unpooled;
import mc.rpgstats.command.CheatCommand;
import mc.rpgstats.command.StatsCommand;
import mc.rpgstats.component.internal.PlayerPreferencesComponent;
import mc.rpgstats.event.LevelUpCallback;
import mc.rpgstats.mixin_logic.OnSneakLogic;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
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
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import static mc.rpgstats.main.RPGStats.softLevelUp;

public class Events {
    private static int tickCount = 0;
    
    public static void registerCommandRegisters() {
        // Commands
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, dedicated) -> {
                StatsCommand.register(dispatcher);
                CheatCommand.register(dispatcher);
            }
        );
    }
    
    public static void registerResourceReloadListeners() {
        // Data driven stuff
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("rpgstats:stats");
            }
        
            @Override
            public void apply(ResourceManager manager) {
                CustomComponents.components.clear();
            
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
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 13 * 20));
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 2 * 20, 1));
                        }
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
                    player.sendMessage(new LiteralText("§a+0.01§r Knockback resistance"), false);
                if (newLevel % 2 == 0 && newLevel > 10) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH) + 1);
                    if (!hideMessages)
                        player.sendMessage(new LiteralText("§a+1§r Health"), false);
                }
    
                if (!hideMessages) {
                    if (newLevel == 25) {
                        player.sendMessage(new LiteralText("§aNimble§r - 5% chance to avoid damage"), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(new LiteralText("§aNimble II§r - 10% chance to avoid damage"), false);
                    }
                }
            }
        });
    
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.FARMING)) {
                if (!hideMessages) {
                    player.sendMessage(new LiteralText("§a+1§r Bonemeal efficiency"), false);
        
                    if (newLevel == 25) {
                        player.sendMessage(new LiteralText("§aNurturing§r - Shift rapidly to grow nearby crops (while holding hoe)"), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(new LiteralText("§aNurturing II§r - Nurturing has increased range"), false);
                    }
                }
            }
        });
    
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.FISHING)) {
                player.getAttributeInstance(EntityAttributes.GENERIC_LUCK).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_LUCK) + 0.05);
    
                if (!hideMessages) {
                    player.sendMessage(new LiteralText("§a+0.05§r Luck"), false);
                    if (newLevel == 25) {
                        player.sendMessage(new LiteralText("§aVitamin rich§r - Eating fish grants you a temporary positive effect"), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(new LiteralText("§aTeach a man to fish§r - Extra saturation when eating"), false);
                    }
                }
            }
        });
    
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.MAGIC)) {
                if (!hideMessages) {
                    player.sendMessage(new LiteralText("§a+1§r Drunk potion duration"), false);
        
                    if (newLevel % 3 == 0) {
                        player.sendMessage(new LiteralText("§a+1§r Potion drink speed"), false);
                    }
        
                    if (newLevel == 25) {
                        player.sendMessage(new LiteralText("§aVax§r - Immune to poison"), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(new LiteralText("§aDead inside§r - Immune to wither"), false);
                    }
                }
            }
        });
    
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.MELEE)) {
                player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) + 0.08);
    
                if (!hideMessages) {
                    player.sendMessage(new LiteralText("§a+0.08§r Melee damage"), false);
                    if (newLevel == 25) {
                        player.sendMessage(new LiteralText("§aBloodthirst§r - Regain 1 heart after killing a monster"), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(new LiteralText("§aBloodthirst II§r - Regain 2 hearts after killing a monster"), false);
                    }
                }
            }
        });
    
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.MINING)) {
                player.getAttributeInstance(EntityAttributes.GENERIC_LUCK).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_LUCK) + 0.05);
    
                if (!hideMessages) {
                    player.sendMessage(new LiteralText("§a+0.05§r Luck"), false);
        
                    if (newLevel == 25) {
                        player.sendMessage(new LiteralText("§aMagically infused§r - Extra 5% chance to not consume durability with unbreaking."), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(new LiteralText("§aMiners sight§r - Night vision and haste below y40"), false);
                    }
                }
            }
        });
    
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.RANGED)) {
                if (!hideMessages) {
                    player.sendMessage(new LiteralText("§a+1§r Bow accuracy"), false);
        
                    if (newLevel == 25) {
                        player.sendMessage(new LiteralText("§aAqueus§r - Impaling applies to all mobs, not just water based ones"), false);
                    } else if (newLevel == 50) {
                        player.sendMessage(new LiteralText("§aNix§r - You no longer need arrows"), false);
                    }
                }
            }
        });
    }
    
    public static void registerBlockBreakListeners() {
        PlayerBlockBreakEvents.AFTER.register((world, playerEntity, blockPos, blockState, blockEntity) -> {
            if (!world.isClient) {
                Block block = blockState.getBlock();
                if (block instanceof PlantBlock || block instanceof PumpkinBlock || block instanceof MelonBlock || block instanceof CocoaBlock) {
                    if (block instanceof CropBlock) {
                        if (((CropBlock)block).isMature(blockState)) {
                            RPGStats.addXpAndLevelUp(CustomComponents.FARMING, (ServerPlayerEntity)playerEntity, 1);
                        }
                    } else {
                        RPGStats.addXpAndLevelUp(CustomComponents.FARMING, (ServerPlayerEntity)playerEntity, 1);
                    }
                }
        
                Random random = new Random();
                if ((block == Blocks.ANCIENT_DEBRIS || block instanceof OreBlock) && random.nextBoolean()) {
                    int amount;
                    if (
                        block == Blocks.COAL_ORE ||
                            block == Blocks.NETHER_GOLD_ORE
                    ) {
                        amount = 1;
                    } else if (
                        block == Blocks.IRON_ORE ||
                            block == Blocks.NETHER_QUARTZ_ORE
                    ) {
                        amount = 2;
                    } else if (
                        block == Blocks.GOLD_ORE ||
                            block == Blocks.LAPIS_ORE ||
                            block == Blocks.REDSTONE_ORE
                    ) {
                        amount = 3;
                    } else if (block == Blocks.EMERALD_ORE) {
                        amount = 4;
                    } else if (
                        block == Blocks.DIAMOND_ORE ||
                            block == Blocks.ANCIENT_DEBRIS
                    ) {
                        amount = 5;
                    } else {
                        amount = 2;
                    }
                    RPGStats.addXpAndLevelUp(CustomComponents.MINING, (ServerPlayerEntity)playerEntity, amount);
                }
            }
        });
    }
}

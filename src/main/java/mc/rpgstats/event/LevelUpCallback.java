package mc.rpgstats.event;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import mc.rpgstats.component.IStatComponent;
import mc.rpgstats.main.RPGStats;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Callback for player level up
 * Called after messages are sent, and is purely a way to listen, you can not influence the level up
 * Lambda params - PlayerEntity player, ComponentType<? extends IStatComponent> type, int newLevel
 */
public interface LevelUpCallback {
    Event<LevelUpCallback> EVENT = EventFactory.createArrayBacked(LevelUpCallback.class,
        (listeners) -> (player, type, newLevel) -> {
            RPGStats.levelUpCriterion.trigger((ServerPlayerEntity)player);
            for (LevelUpCallback listener : listeners) {
                listener.onLevelUp(player, type, newLevel);
            }
        });
    
    void onLevelUp(PlayerEntity player, ComponentKey<? extends IStatComponent> type, int newLevel);
}

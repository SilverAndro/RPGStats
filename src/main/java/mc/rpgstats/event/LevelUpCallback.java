package mc.rpgstats.event;

import mc.rpgstats.component.IStatComponent;
import nerdhub.cardinal.components.api.ComponentType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Callback for player level up
 * Called after messages are sent, and is purely a way to listen, you can not influence the level up
 * Lambda params - PlayerEntity player, ComponentType<? extends IStatComponent> type, int newLevel
 */
public interface LevelUpCallback {
    Event<LevelUpCallback> EVENT = EventFactory.createArrayBacked(LevelUpCallback.class,
        (listeners) -> (player, type, newLevel) -> {
            for (LevelUpCallback listener : listeners) {
                listener.onLevelUp(player, type, newLevel);
            }
        });
    
    void onLevelUp(PlayerEntity player, ComponentType<? extends IStatComponent> type, int newLevel);
}

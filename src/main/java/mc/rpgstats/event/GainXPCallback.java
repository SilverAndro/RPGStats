package mc.rpgstats.event;

import mc.rpgstats.component.IStatComponent;
import nerdhub.cardinal.components.api.ComponentType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Callback for player gaining xp
 * Called after xp is granted, and is purely a way to listen, you can not influence the xp granted
 * Lambda params - PlayerEntity player, ComponentType<? extends IStatComponent> type, int newXP
 */
public interface GainXPCallback {
    Event<GainXPCallback> EVENT = EventFactory.createArrayBacked(GainXPCallback.class,
        (listeners) -> (player, type, newLevel) -> {
            for (GainXPCallback listener : listeners) {
                listener.onGainXP(player, type, newLevel);
            }
        });
    
    void onGainXP(PlayerEntity player, ComponentType<? extends IStatComponent> type, int newXp);
}

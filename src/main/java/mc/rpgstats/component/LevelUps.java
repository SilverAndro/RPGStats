package mc.rpgstats.component;

import mc.rpgstats.event.LevelUpCallback;
import mc.rpgstats.main.CustomComponents;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.LiteralText;

import java.util.Objects;

public class LevelUps {
    public static void registerLevelUpEvents() {
        LevelUpCallback.EVENT.register((player, id, newLevel, hideMessages) -> {
            if (id.equals(CustomComponents.DEFENSE)) {
                player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) + 0.01);
                if (!hideMessages)
                    player.sendMessage(new LiteralText("§a+0.01§r Knockback resistance"), false);
                if (newLevel % 2 == 0 && newLevel > 10) {
                    Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH) + 1);
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
    }
}

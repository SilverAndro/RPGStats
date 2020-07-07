package mc.rpgstats.component;

import dev.onyxstudios.cca.api.v3.util.PlayerComponent;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.entity.Entity;

public interface IStatComponent extends PlayerComponent<Component> {
    int getXP();
    void setXP(int newXP);
    int getLevel();
    void setLevel(int newLevel);
    String getName();
    String getCapName();
    void onLevelUp();
    Entity getEntity();
}

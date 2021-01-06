package mc.rpgstats.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.minecraft.entity.Entity;

public interface IStatComponent extends PlayerComponent<Component> {
    int getXP();
    void setXP(int newXP);
    int getLevel();
    void setLevel(int newLevel);
    String getName();
    String getCapName();
    void onLevelUp(boolean beQuiet);
    Entity getEntity();
}

package mc.rpgstats.components;

import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;
import net.minecraft.server.network.ServerPlayerEntity;

public interface IStatComponent extends EntitySyncedComponent {
    int getXP();
    void setXP(int newXP);
    int getLevel();
    void setLevel(int newLevel);
    String getName();
    void onLevelUp();
}

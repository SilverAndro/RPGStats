package mc.rpgstats.component;

import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;

public interface IStatComponent extends EntitySyncedComponent {
    int getXP();
    void setXP(int newXP);
    int getLevel();
    void setLevel(int newLevel);
    String getName();
    String getCapName();
    void onLevelUp();
}

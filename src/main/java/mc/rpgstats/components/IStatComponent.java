package mc.rpgstats.components;

import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;

interface IStatComponent extends EntitySyncedComponent {
    int getValue();
    void setValue(int newValue);
}

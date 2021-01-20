package mc.rpgstats.main;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import mc.rpgstats.component.*;
import mc.rpgstats.component.internal.PlayerPreferencesComponent;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomComponents implements EntityComponentInitializer {
    public static ArrayList<ComponentKey<? extends IStatComponent>> statList = new ArrayList<>();
    public static HashMap<Identifier, Integer> idToComponentIndexMap = new HashMap<>();
    
    public static ComponentKey<MeleeComponent> MELEE_COMPONENT;
    public static ComponentKey<RangedComponent> RANGED_COMPONENT;
    public static ComponentKey<DefenseComponent> DEFENSE_COMPONENT;
    public static ComponentKey<FarmingComponent> FARMING_COMPONENT;
    public static ComponentKey<MagicComponent> MAGIC_COMPONENT;
    public static ComponentKey<MiningComponent> MINING_COMPONENT;
    public static ComponentKey<FishingComponent> FISHING_COMPONENT;
    
    public static ComponentKey<PlayerPreferencesComponent> PREFERENCES;
    
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        MELEE_COMPONENT = registerSkill(new Identifier("rpgstats:melee"), MeleeComponent.class, registry);
        RANGED_COMPONENT = registerSkill(new Identifier("rpgstats:ranged"), RangedComponent.class, registry);
        DEFENSE_COMPONENT = registerSkill(new Identifier("rpgstats:defence"), DefenseComponent.class, registry);
        FARMING_COMPONENT = registerSkill(new Identifier("rpgstats:farming"), FarmingComponent.class, registry);
        MAGIC_COMPONENT = registerSkill(new Identifier("rpgstats:magic"), MagicComponent.class, registry);
        MINING_COMPONENT = registerSkill(new Identifier("rpgstats:mining"), MiningComponent.class, registry);
        FISHING_COMPONENT = registerSkill(new Identifier("rpgstats:fishing"), FishingComponent.class, registry);
        
        PREFERENCES = ComponentRegistry.getOrCreate(new Identifier("rpgstats:internal"), PlayerPreferencesComponent.class);
        registry.beginRegistration(PlayerEntity.class, PREFERENCES).impl(PlayerPreferencesComponent.class).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(PlayerPreferencesComponent::new);
        
        if (RPGStats.getConfig().hardcoreMode) {
            registry.beginRegistration(PlayerEntity.class, MELEE_COMPONENT).impl(MeleeComponent.class).end(MeleeComponent::new);
            registry.beginRegistration(PlayerEntity.class, RANGED_COMPONENT).impl(RangedComponent.class).end(RangedComponent::new);
            registry.beginRegistration(PlayerEntity.class, DEFENSE_COMPONENT).impl(DefenseComponent.class).end(DefenseComponent::new);
            registry.beginRegistration(PlayerEntity.class, FARMING_COMPONENT).impl(FarmingComponent.class).end(FarmingComponent::new);
            registry.beginRegistration(PlayerEntity.class, MAGIC_COMPONENT).impl(MagicComponent.class).end(MagicComponent::new);
            registry.beginRegistration(PlayerEntity.class, MINING_COMPONENT).impl(MiningComponent.class).end(MiningComponent::new);
            registry.beginRegistration(PlayerEntity.class, FISHING_COMPONENT).impl(FishingComponent.class).end(FishingComponent::new);
        } else {
            registry.beginRegistration(PlayerEntity.class, MELEE_COMPONENT).impl(MeleeComponent.class).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(MeleeComponent::new);
            registry.beginRegistration(PlayerEntity.class, RANGED_COMPONENT).impl(RangedComponent.class).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(RangedComponent::new);
            registry.beginRegistration(PlayerEntity.class, DEFENSE_COMPONENT).impl(DefenseComponent.class).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(DefenseComponent::new);
            registry.beginRegistration(PlayerEntity.class, FARMING_COMPONENT).impl(FarmingComponent.class).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(FarmingComponent::new);
            registry.beginRegistration(PlayerEntity.class, MAGIC_COMPONENT).impl(MagicComponent.class).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(MagicComponent::new);
            registry.beginRegistration(PlayerEntity.class, MINING_COMPONENT).impl(MiningComponent.class).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(MiningComponent::new);
            registry.beginRegistration(PlayerEntity.class, FISHING_COMPONENT).impl(FishingComponent.class).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(FishingComponent::new);
        }
    }
    
    public static <T extends IStatComponent> ComponentKey<T> registerSkill(Identifier componentID, Class<T> componentClass, EntityComponentFactoryRegistry registry) {
        ComponentKey<T> componentType = ComponentRegistry.getOrCreate(componentID, componentClass);
        statList.add(componentType);
        idToComponentIndexMap.put(componentID, statList.indexOf(componentType));
        return componentType;
    }
}

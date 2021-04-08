package mc.rpgstats.main;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import mc.rpgstats.component.*;
import mc.rpgstats.component.internal.PlayerHealthAttachComponent;
import mc.rpgstats.component.internal.PlayerPreferencesComponent;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomComponents implements EntityComponentInitializer {
    public static HashMap<Identifier, String> customComponents = new HashMap<>();
    
    public static ArrayList<ComponentKey<? extends IStatComponent>> oldComponentStatList = new ArrayList<>();
    public static HashMap<Identifier, Integer> oldComponentIdToComponentIndexMap = new HashMap<>();
    
    public static ComponentKey<MeleeComponent> MELEE_COMPONENT;
    public static ComponentKey<RangedComponent> RANGED_COMPONENT;
    public static ComponentKey<DefenseComponent> DEFENSE_COMPONENT;
    public static ComponentKey<FarmingComponent> FARMING_COMPONENT;
    public static ComponentKey<MagicComponent> MAGIC_COMPONENT;
    public static ComponentKey<MiningComponent> MINING_COMPONENT;
    public static ComponentKey<FishingComponent> FISHING_COMPONENT;
    
    public static ComponentKey<StatsComponent> STATS;
    public static ComponentKey<PlayerPreferencesComponent> PREFERENCES;
    public static ComponentKey<PlayerHealthAttachComponent> MAX_HEALTH;
    
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        MELEE_COMPONENT = registerSkill(new Identifier("rpgstats:melee"), MeleeComponent.class);
        RANGED_COMPONENT = registerSkill(new Identifier("rpgstats:ranged"), RangedComponent.class);
        DEFENSE_COMPONENT = registerSkill(new Identifier("rpgstats:defence"), DefenseComponent.class);
        FARMING_COMPONENT = registerSkill(new Identifier("rpgstats:farming"), FarmingComponent.class);
        MAGIC_COMPONENT = registerSkill(new Identifier("rpgstats:magic"), MagicComponent.class);
        MINING_COMPONENT = registerSkill(new Identifier("rpgstats:mining"), MiningComponent.class);
        FISHING_COMPONENT = registerSkill(new Identifier("rpgstats:fishing"), FishingComponent.class);
        
        STATS = ComponentRegistry.getOrCreate(new Identifier("rpgstats:stats"), StatsComponent.class);
        
        PREFERENCES = ComponentRegistry.getOrCreate(new Identifier("rpgstats:internal"), PlayerPreferencesComponent.class);
        registry.registerForPlayers(PREFERENCES, PlayerPreferencesComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        MAX_HEALTH = ComponentRegistry.getOrCreate(new Identifier("rpgstats:max_health"), PlayerHealthAttachComponent.class);
        registry.registerForPlayers(MAX_HEALTH, PlayerHealthAttachComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        
        if (RPGStats.getConfig().hardcoreMode) {
            registry.registerForPlayers(STATS, StatsComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
            
            registry.registerForPlayers(MELEE_COMPONENT, MeleeComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
            registry.registerForPlayers(RANGED_COMPONENT, RangedComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
            registry.registerForPlayers(DEFENSE_COMPONENT, DefenseComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
            registry.registerForPlayers(FARMING_COMPONENT, FarmingComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
            registry.registerForPlayers(MAGIC_COMPONENT, MagicComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
            registry.registerForPlayers(MINING_COMPONENT, MiningComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
            registry.registerForPlayers(FISHING_COMPONENT, FishingComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        } else {
            registry.registerForPlayers(STATS, StatsComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
            
            registry.registerForPlayers(MELEE_COMPONENT, MeleeComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
            registry.registerForPlayers(RANGED_COMPONENT, RangedComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
            registry.registerForPlayers(DEFENSE_COMPONENT, DefenseComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
            registry.registerForPlayers(FARMING_COMPONENT, FarmingComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
            registry.registerForPlayers(MAGIC_COMPONENT, MagicComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
            registry.registerForPlayers(MINING_COMPONENT, MiningComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
            registry.registerForPlayers(FISHING_COMPONENT, FishingComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        }
    }
    
    public static <T extends IStatComponent> ComponentKey<T> registerSkill(Identifier componentID, Class<T> componentClass) {
        ComponentKey<T> componentType = ComponentRegistry.getOrCreate(componentID, componentClass);
        oldComponentStatList.add(componentType);
        oldComponentIdToComponentIndexMap.put(componentID, oldComponentStatList.indexOf(componentType));
        return componentType;
    }
}

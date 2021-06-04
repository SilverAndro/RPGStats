package mc.rpgstats.main;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import mc.rpgstats.component.*;
import mc.rpgstats.component.internal.PlayerHealthAttachComponent;
import mc.rpgstats.component.internal.PlayerPreferencesComponent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomComponents implements EntityComponentInitializer {
    public static HashMap<Identifier, String> components = new HashMap<>();
    
    public static Identifier MELEE = new Identifier("rpgstats:melee");
    public static Identifier RANGED = new Identifier("rpgstats:ranged");
    public static Identifier DEFENSE = new Identifier("rpgstats:defence");
    public static Identifier FARMING = new Identifier("rpgstats:farming");
    public static Identifier MAGIC = new Identifier("rpgstats:magic");
    public static Identifier MINING = new Identifier("rpgstats:mining");
    public static Identifier FISHING = new Identifier("rpgstats:fishing");
    
    public static ComponentKey<StatsComponent> STATS;
    public static ComponentKey<PlayerPreferencesComponent> PREFERENCES;
    public static ComponentKey<PlayerHealthAttachComponent> MAX_HEALTH;
    
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        PREFERENCES = ComponentRegistry.getOrCreate(new Identifier("rpgstats:internal"), PlayerPreferencesComponent.class);
        registry.registerForPlayers(PREFERENCES, PlayerPreferencesComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        MAX_HEALTH = ComponentRegistry.getOrCreate(new Identifier("rpgstats:max_health"), PlayerHealthAttachComponent.class);
        registry.registerForPlayers(MAX_HEALTH, PlayerHealthAttachComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        
        STATS = ComponentRegistry.getOrCreate(new Identifier("rpgstats:stats"), StatsComponent.class);
        if (RPGStats.getConfig().hardcoreMode) {
            registry.registerForPlayers(STATS, StatsComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        } else {
            registry.registerForPlayers(STATS, StatsComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        }
    }
    
}

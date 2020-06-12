package mc.rpgstats.main;

import mc.rpgstats.components.*;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class RPGStats implements ModInitializer {
	// Stat components
	public static final ComponentType<MeleeComponent> MELEE_COMPONENT = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier("rpgstats:melee"), MeleeComponent.class);
	public static final ComponentType<RangedComponent> RANGED_COMPONENT = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier("rpgstats:ranged"), RangedComponent.class);
	public static final ComponentType<DefenceComponent> DEFENCE_COMPONENT = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier("rpgstats:defence"), DefenceComponent.class);
	public static final ComponentType<FarmingComponent> FARMING_COMPONENT = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier("rpgstats:farming"), FarmingComponent.class);
	public static final ComponentType<MagicComponent> MAGIC_COMPONENT = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier("rpgstats:magic"), MagicComponent.class);

	@Override
	public void onInitialize() {
		// Init components on players
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(MELEE_COMPONENT, new MeleeComponent()));
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(RANGED_COMPONENT, new RangedComponent()));
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(DEFENCE_COMPONENT, new DefenceComponent()));
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(FARMING_COMPONENT, new FarmingComponent()));
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(MAGIC_COMPONENT, new MagicComponent()));

		// Keeps stats always
		EntityComponents.setRespawnCopyStrategy(MELEE_COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
		EntityComponents.setRespawnCopyStrategy(RANGED_COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
		EntityComponents.setRespawnCopyStrategy(DEFENCE_COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
		EntityComponents.setRespawnCopyStrategy(FARMING_COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
		EntityComponents.setRespawnCopyStrategy(MAGIC_COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
	}
}

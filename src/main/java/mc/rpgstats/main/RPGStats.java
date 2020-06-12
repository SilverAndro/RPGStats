package mc.rpgstats.main;

import mc.rpgstats.components.*;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

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
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(MELEE_COMPONENT, new MeleeComponent(player)));
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(RANGED_COMPONENT, new RangedComponent(player)));
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(DEFENCE_COMPONENT, new DefenceComponent(player)));
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(FARMING_COMPONENT, new FarmingComponent(player)));
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(MAGIC_COMPONENT, new MagicComponent(player)));

		// Keeps stats always
		EntityComponents.setRespawnCopyStrategy(MELEE_COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
		EntityComponents.setRespawnCopyStrategy(RANGED_COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
		EntityComponents.setRespawnCopyStrategy(DEFENCE_COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
		EntityComponents.setRespawnCopyStrategy(FARMING_COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
		EntityComponents.setRespawnCopyStrategy(MAGIC_COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
	}

	// Helper methods for components
	public static void setComponentXP(ComponentType<? extends IStatComponent> type, ComponentProvider provider, int newValue) {
		type.get(provider).setXP(newValue);
	}

	public static int getComponentXP(ComponentType<? extends IStatComponent> type, ComponentProvider provider) {
		return type.get(provider).getXP();
	}

	public static void setComponentLevel(ComponentType<? extends IStatComponent> type, ComponentProvider provider, int newValue) {
		type.get(provider).setLevel(newValue);
	}

	public static int getComponentLevel(ComponentType<? extends IStatComponent> type, ComponentProvider provider) {
		return type.get(provider).getLevel();
	}

	public static void addXpAndLevelUpIfNeeded(ComponentType<? extends IStatComponent> type, ComponentProvider provider, int newValue) {
		int nextXP = type.get(provider).getXP() + newValue;
		int currentlevel = getComponentLevel(type, provider);

		if (currentlevel <= 51) {
			// Enough to level up
			double nextXPForLevelUp = Math.floor(Math.pow(currentlevel + 1, 3) * 0.08) + 50;
			if (nextXP >= nextXPForLevelUp) {
				nextXP -= nextXPForLevelUp;
				setComponentLevel(type, provider, currentlevel + 1);
				((PlayerEntity)type.get(provider).getEntity()).sendMessage(new LiteralText("You leveled up your ___ stat!"), false);
			}

			setComponentXP(type, provider, nextXP);
		}
	}
}
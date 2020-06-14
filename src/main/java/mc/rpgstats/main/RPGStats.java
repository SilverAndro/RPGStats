package mc.rpgstats.main;

import mc.rpgstats.command.StatsCommand;
import mc.rpgstats.components.*;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
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

		// Command
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			StatsCommand.register(dispatcher);
		});
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

	public static int calculateXpNeededToReachLevel(int level) {
		return (int)Math.floor(Math.pow(level, 3) * 0.08) + 70;
	}

	public static void addXpAndLevelUpIfNeeded(ComponentType<? extends IStatComponent> type, ComponentProvider provider, int addedXP) {
		int nextXP = getComponentXP(type, provider) + addedXP;
		int currentLevel = getComponentLevel(type, provider);

		if (currentLevel <= 50) {
			// Enough to level up
			int nextXPForLevelUp = calculateXpNeededToReachLevel(currentLevel + 1);
			if (nextXP >= nextXPForLevelUp) {
				nextXP -= nextXPForLevelUp;
				setComponentLevel(type, provider, currentLevel + 1);
				((PlayerEntity)type.get(provider).getEntity()).sendMessage(new LiteralText("§aRPGStats >§r You gained a §6" + type.get(provider).getName() + "§r level! You are now level §6" + type.get(provider).getLevel()), false);
				type.get(provider).onLevelUp();
			}

			setComponentXP(type, provider, nextXP);
		}
	}

	public static String getFormattedLevelData(ComponentType<? extends IStatComponent> type, ComponentProvider provider) {
		int currentLevel = getComponentLevel(type, provider);
		int xp = getComponentXP(type, provider);
		if (currentLevel < 50) {
			int nextXP = calculateXpNeededToReachLevel(currentLevel + 1);
			return "§6" + type.get(provider).getCapName() + "§r - Level: " + currentLevel + " XP: " + xp + "/" + nextXP;
		} else {
			return "§6" + type.get(provider).getCapName() + "§r - Level: " + currentLevel;
		}
	}
}
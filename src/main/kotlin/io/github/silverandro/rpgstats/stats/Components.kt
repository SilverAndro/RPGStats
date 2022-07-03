package io.github.silverandro.rpgstats.stats

import com.mojang.serialization.Lifecycle
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import io.github.silverandro.rpgstats.stats.internal.PlayerHealthAttachComponent
import io.github.silverandro.rpgstats.stats.internal.PlayerPreferencesComponent
import io.github.silverandro.rpgstats.stats.systems.StatAction
import io.github.silverandro.rpgstats.stats.systems.StatAttributeAction
import io.github.silverandro.rpgstats.stats.systems.StatFakeAttributeAction
import io.github.silverandro.rpgstats.stats.systems.StatSpecialAction
import mc.rpgstats.main.RPGStats
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry
import org.quiltmc.qkl.wrapper.qsl.registry.register

class Components : EntityComponentInitializer {
    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        PREFERENCES = ComponentRegistry
            .getOrCreate(
                Identifier("rpgstats:internal"),
                PlayerPreferencesComponent::class.java
            )
        registry.registerForPlayers(
            PREFERENCES,
            { playerEntity -> PlayerPreferencesComponent(playerEntity) },
            RespawnCopyStrategy.ALWAYS_COPY
        )

        MAX_HEALTH = ComponentRegistry
            .getOrCreate(
                Identifier("rpgstats:max_health"),
                PlayerHealthAttachComponent::class.java
            )
        registry.registerForPlayers(
            MAX_HEALTH,
            { playerEntity -> PlayerHealthAttachComponent(playerEntity) },
            RespawnCopyStrategy.LOSSLESS_ONLY
        )

        STATS = ComponentRegistry.getOrCreate(
            Identifier("rpgstats:stats"),
            StatsComponent::class.java
        )
        if (RPGStats.getConfig().hardcoreMode) {
            registry.registerForPlayers(
                STATS,
                { playerEntity -> StatsComponent(playerEntity) },
                RespawnCopyStrategy.LOSSLESS_ONLY
            )
        } else {
            registry.registerForPlayers(
                STATS,
                { playerEntity -> StatsComponent(playerEntity) },
                RespawnCopyStrategy.ALWAYS_COPY
            )
        }
    }

    companion object {
        @JvmField
        var components = HashMap<Identifier, String>()
        val actions: SimpleRegistry<Array<StatAction>> = FabricRegistryBuilder.createSimple(
            Array<StatAction>::class.java,
            Identifier("rpgstats:actions")
        ).buildAndRegister()

        fun registerStat(id: Identifier, vararg action: StatAction): Identifier {
            val stat = StatEntry(
                "rpgstats.stat.${id.path.replace("/", "_")}",
                id
            )

            @Suppress("UNCHECKED_CAST")
            Registry.register(actions, id, action as Array<StatAction>)

            return stat.id
        }

        val MELEE = registerStat(
            Identifier("rpgstats:melee"),
            StatAttributeAction(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                RPGStats.getConfig().melee.attackDamagePerLevel
            ) { true },
            StatSpecialAction(
                "Bloodthirst",
                "Regain 1 heart after killing a monster"
            ) { it == 25 },
            StatSpecialAction(
                "Bloodthirst II",
                "Regain 2 hearts after killing a monster"
            ) { it == 50 }
        )

        val MINING = registerStat(
            Identifier("rpgstats:mining"),
            StatFakeAttributeAction(
                "rpgstats.fakestat.mining_speed",
                0.1
            ) { true },
            StatSpecialAction(
                "Magically infused",
                "Extra 5% chance to not consume durability with unbreaking"
            ) { it == 25 },
            StatSpecialAction(
                "Miners sight",
                "Night vision below y" + RPGStats.getConfig().toggles.mining.effectLevelTrigger
            ) { it == 50 }
        )

        val RANGED = registerStat(
            Identifier("rpgstats:ranged"),
            StatFakeAttributeAction(
                "rpgstats.fakestat.bow_accuracy",
                1.0
            ) { true },
            StatSpecialAction(
                "Aqueus",
                "Impaling applies to all mobs, not just water based ones"
            ) { it == 25 },
            StatSpecialAction(
                "Nix",
                "You no longer need arrows"
            ) { it == 50 }
        )

        @JvmStatic
        lateinit var STATS: ComponentKey<StatsComponent>

        @JvmStatic
        lateinit var PREFERENCES: ComponentKey<PlayerPreferencesComponent>

        @JvmStatic
        lateinit var MAX_HEALTH: ComponentKey<PlayerHealthAttachComponent>
    }
}
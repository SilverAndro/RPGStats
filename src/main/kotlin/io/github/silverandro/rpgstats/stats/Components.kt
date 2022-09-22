package io.github.silverandro.rpgstats.stats

import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import io.github.silverandro.rpgstats.RPGStatsMain
import io.github.silverandro.rpgstats.stats.internal.PlayerPreferencesComponent
import io.github.silverandro.rpgstats.stats.systems.StatAction
import io.github.silverandro.rpgstats.stats.systems.StatAttributeAction
import io.github.silverandro.rpgstats.stats.systems.StatFakeAttributeAction
import io.github.silverandro.rpgstats.stats.systems.StatSpecialAction
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.random.RandomGenerator
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.SimpleRegistry
import java.util.UUID

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

        STATS = ComponentRegistry.getOrCreate(
            Identifier("rpgstats:stats"),
            StatsComponent::class.java
        )
        if (RPGStatsMain.config.hardcoreMode) {
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

        private fun registerStat(id: Identifier, vararg action: StatAction): Identifier {
            @Suppress("UNCHECKED_CAST")
            Registry.register(actions, id, action as Array<StatAction>)

            return id
        }

        @JvmField
        val MELEE = registerStat(
            Identifier("rpgstats:melee"),
            StatAttributeAction(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                RPGStatsMain.levelConfig.melee.attackDamagePerLevel
            ) { true },
            StatSpecialAction(
                "rpgstats.special.bloodthirst",
                "rpgstats.special.bloodthirst.description",
                1
            ) { it == 25 },
            StatSpecialAction(
                "rpgstats.special.bloodthirst_2",
                "rpgstats.special.bloodthirst.description",
                2
            ) { it == 50 }
        )

        @JvmField
        val MINING = registerStat(
            Identifier("rpgstats:mining"),
            StatFakeAttributeAction(
                "rpgstats.fakestat.mining_speed",
                0.1
            ) { true },
            StatSpecialAction(
                "rpgstats.special.magical_infusion",
                "rpgstats.special.magical_infusion.description"
            ) { it == 25 },
            StatSpecialAction(
                "rpgstats.special.miner_sight",
                "rpgstats.special.miner_sight.description",
                RPGStatsMain.levelConfig.mining.effectLevelTrigger
            ) { it == 50 }
        )

        @JvmField
        val RANGED = registerStat(
            Identifier("rpgstats:ranged"),
            StatFakeAttributeAction(
                "rpgstats.fakestat.bow_accuracy",
                1.0
            ) { true },
            StatSpecialAction(
                "rpgstats.special.aqueus",
                "rpgstats.special.aqueus.description"
            ) { it == 25 },
            StatSpecialAction(
                "rpgstats.special.nix",
                "rpgstats.special.nix.description"
            ) { it == 50 }
        )

        @JvmField
        val MAGIC = registerStat(
            Identifier("rpgstats:magic"),
            StatFakeAttributeAction(
                "rpgstats.fakestat.drunk_potion_duration",
                1.0
            ) { true },
            StatFakeAttributeAction(
                "rpgstats.fakestat.potion_drink_speed",
                1.0
            ) { it % 3 == 0 },
            StatSpecialAction(
                "rpgstats.special.vax",
                "rpgstats.special.vax.description"
            ) { it == 25 },
            StatSpecialAction(
                "rpgstats.special.dead_inside",
                "rpgstats.special.dead_inside.description"
            ) { it == 50 }
        )

        @JvmField
        val FARMING = registerStat(
            Identifier("rpgstats:farming"),
            StatFakeAttributeAction(
                "rpgstats.fakestat.bonemeal_efficiency",
                1.0
            ) { true },
            StatSpecialAction(
                "Nurturing",
                "Shift rapidly to grow nearby crops (while holding a hoe)"
            ) { it == 25 },
            StatSpecialAction(
                "Nurturing II",
                "Nurturing has increased range"
            ) { it == 50 }
        )

        @JvmField
        val DEFENCE = registerStat(
            Identifier("rpgstats:defence"),
            StatAttributeAction(
                EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                0.01
            ) { true },
            StatAttributeAction(
                EntityAttributes.GENERIC_MAX_HEALTH,
                RPGStatsMain.levelConfig.defense.addAmount.toDouble()
            ) { it % RPGStatsMain.levelConfig.defense.everyXLevels == 0 && it > RPGStatsMain.levelConfig.defense.afterLevel },
            StatSpecialAction(
                "Nimble",
                "5% chance to avoid damage"
            ) { it == 25 },
            StatSpecialAction(
                "Nimble II",
                "10% chance to avoid damage"
            ) { it == 50 }
        )

        @JvmField
        val FISHING = registerStat(
            Identifier("rpgstats:fishing"),
            StatAttributeAction(
                EntityAttributes.GENERIC_LUCK,
                0.05
            ) { true },
            StatSpecialAction(
                "Vitamin rich",
                "Eating fish grants you a temporary positive effect"
            ) { it == 25 },
            StatSpecialAction(
                "Teach a man to fish",
                "Extra saturation when eating"
            ) { it == 50 }
        )

        @JvmStatic
        lateinit var STATS: ComponentKey<StatsComponent>

        @JvmStatic
        lateinit var PREFERENCES: ComponentKey<PlayerPreferencesComponent>

        private val modifierIDs = mutableMapOf<String, UUID>()
        fun modifierIDFor(name: String, index: Int): UUID {
            return modifierIDs.computeIfAbsent("$name$index") {
                MathHelper.m_vkfnsave(RandomGenerator.createThreaded())
            }
        }
    }
}
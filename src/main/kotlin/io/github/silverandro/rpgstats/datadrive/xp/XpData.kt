package io.github.silverandro.rpgstats.datadrive.xp

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment

object XpData {
    data class XpEntry(val id: Identifier, val amount: Int)

    private val recordCodec: Codec<XpEntry> = RecordCodecBuilder.create { instance ->
        instance.group(
            Identifier.CODEC.fieldOf("id").forGetter { it.id },
            Codec.INT.fieldOf("amount").forGetter { it.amount }
        ).apply(instance) { id: Identifier, amount: Int ->
            XpEntry(id, amount)
        }
    }

    val BLOCK_XP = makeREA(Registry.BLOCK, "block")
    val ENTITY_XP = makeREA(Registry.ENTITY_TYPE, "entity")

    private fun <T> makeREA(registry: Registry<T>, name: String): RegistryEntryAttachment<T, Either<XpEntry, MutableList<XpEntry>>> {
        @Suppress("UNCHECKED_CAST")
        return RegistryEntryAttachment.builder(
                registry,
                Identifier("rpgstats", "${name}_xp"),
                Either::class.java as Class<Either<XpEntry, MutableList<XpEntry>>>,
                Codec.either(recordCodec, Codec.list(recordCodec))
        ).build() as RegistryEntryAttachment<T, Either<XpEntry, MutableList<XpEntry>>>
    }

    fun poke() = Unit
}

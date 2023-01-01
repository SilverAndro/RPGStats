package io.github.silverandro.rpgstats.datadrive.xp

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.quiltmc.qkl.library.serialization.CodecFactory
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment

object XpData {
    @Serializable
    data class XpEntry(val id: @Contextual Identifier, val amount: Int, val chance: Double = 1.0)

    private val xpEntryCodec: Codec<XpEntry> = CodecFactory {
        codecs {
            named(Identifier.CODEC, "Identifier")
        }
    }.create()

    val BLOCK_XP = makeREA(Registry.BLOCK, "block")
    val ENTITY_XP_OVERRIDE = makeREA(Registry.ENTITY_TYPE, "entity")

    private fun <T> makeREA(registry: Registry<T>, name: String): RegistryEntryAttachment<T, Either<XpEntry, MutableList<XpEntry>>> {
        @Suppress("UNCHECKED_CAST")
        return RegistryEntryAttachment.builder(
                registry,
                Identifier("rpgstats", "${name}_xp"),
                Either::class.java as Class<Either<XpEntry, MutableList<XpEntry>>>,
                Codec.either(xpEntryCodec, Codec.list(xpEntryCodec))
        ).build() as RegistryEntryAttachment<T, Either<XpEntry, MutableList<XpEntry>>>
    }

    fun poke() = Unit
}

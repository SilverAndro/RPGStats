package io.github.silverandro.rpgstats.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.util.Identifier

object IdentifierSerializer : KSerializer<Identifier> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Identifier", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Identifier) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Identifier {
        val string = decoder.decodeString()
        return Identifier.of(string)
    }
}
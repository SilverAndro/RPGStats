package io.github.silverandro.rpgstats.util

import com.mojang.brigadier.StringReader
import net.minecraft.command.EntitySelectorReader
import net.minecraft.predicate.NumberRange

fun EntitySelectorReader.readSelectorMap(): Map<String, NumberRange.IntRange> {
    val stringReader: StringReader = reader
    val map = mutableMapOf<String, NumberRange.IntRange>()
    stringReader.expect('{')
    stringReader.skipWhitespace()

    while (stringReader.canRead() && stringReader.peek() != '}') {
        stringReader.skipWhitespace()
        val string = stringReader.readQuotedString()
        stringReader.skipWhitespace()
        stringReader.expect('=')
        stringReader.skipWhitespace()
        val intRange = NumberRange.IntRange.parse(stringReader)
        map[string] = intRange
        stringReader.skipWhitespace()
        if (stringReader.canRead() && stringReader.peek() == ',') {
            stringReader.skip()
        }
    }
    stringReader.expect('}')

    setFlag("rpgstatsLevels", true)

    return map
}
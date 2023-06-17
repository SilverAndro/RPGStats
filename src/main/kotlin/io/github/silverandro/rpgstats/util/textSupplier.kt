package io.github.silverandro.rpgstats.util

import com.google.common.base.Supplier
import net.minecraft.text.Text

fun Text.supplier(): Supplier<Text> = Supplier { this }
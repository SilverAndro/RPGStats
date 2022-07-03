package io.github.silverandro.rpgstats.mixin.accessor;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(Criteria.class)
public interface CriteriaAccessor {
    @Accessor(value = "VALUES")
    static Map<Identifier, Criterion<?>> getValues() {
        return null;
    }
}
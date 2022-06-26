package mc.rpgstats.mixin;

import io.github.silverandro.rpgstats.LevelUtils;
import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import mc.rpgstats.main.RPGStatsConfig;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class ApplyDamageMixin {
    private static float originalDamage = 0f;

    @Inject(method = "applyDamage", at = @At("HEAD"))
    public void rpgstats$captureOriginalDamageDealtForXpCalc(DamageSource source, float amount, CallbackInfo ci) {
        originalDamage = amount;
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/player/PlayerEntity;applyEnchantmentsToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F", shift = At.Shift.AFTER))
    public void rpgstats$grantXpFromDamageAbsorbedThroughArmorOrEnchants(DamageSource source, float amount, CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object) this instanceof ServerPlayerEntity && sourceCanGrantXp(source)) {
            float blockedDamage = originalDamage - amount;
            if (blockedDamage <= 3) {
                return;
            }
            LevelUtils.INSTANCE.addXpAndLevelUp(
                    CustomComponents.DEFENSE,
                    (ServerPlayerEntity) (Object) this,
                    Math.min((int) Math.floor(Math.log(Math.pow(blockedDamage, 5.0f))), 4));
        }
    }

    public boolean sourceCanGrantXp(DamageSource source) {
        RPGStatsConfig.DamageSourceBlacklist blacklist = RPGStats.getConfig().damageBlacklist;
        if (source == DamageSource.IN_FIRE) return blacklist.inFire;
        if (source == DamageSource.LIGHTNING_BOLT) return blacklist.lightning;
        if (source == DamageSource.ON_FIRE) return blacklist.onFire;
        if (source == DamageSource.LAVA) return blacklist.lava;
        if (source == DamageSource.HOT_FLOOR) return blacklist.hotFloor;
        if (source == DamageSource.IN_WALL) return blacklist.inWall;
        if (source == DamageSource.CRAMMING) return blacklist.cramming;
        if (source == DamageSource.DROWN) return blacklist.drown;
        if (source == DamageSource.STARVE) return blacklist.starve;
        if (source == DamageSource.CACTUS) return blacklist.cactus;
        if (source == DamageSource.FALL) return blacklist.fall;
        if (source == DamageSource.FLY_INTO_WALL) return blacklist.flyIntoWall;
        if (source == DamageSource.OUT_OF_WORLD) return blacklist.outOfWorld;
        if (source == DamageSource.GENERIC) return blacklist.generic;
        if (source == DamageSource.MAGIC) return blacklist.magic;
        if (source == DamageSource.WITHER) return blacklist.wither;
        if (source == DamageSource.ANVIL) return blacklist.anvil;
        if (source == DamageSource.FALLING_BLOCK) return blacklist.fallingBlock;
        if (source == DamageSource.DRYOUT) return blacklist.dryOut;
        if (source == DamageSource.SWEET_BERRY_BUSH) return blacklist.berryBush;
        if (source == DamageSource.FREEZE) return blacklist.freeze;
        if (source == DamageSource.STALAGMITE) return blacklist.stalactite;
        if (source == DamageSource.FALLING_STALACTITE) return blacklist.fallingStalactite;
        return true;
    }
}
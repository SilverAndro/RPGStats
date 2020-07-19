package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(FishingBobberEntity.class)
public class FishingBobberMixin {
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onCatchItem(ItemStack usedItem, CallbackInfoReturnable<Integer> cir, PlayerEntity playerEntity, int i, LootContext.Builder builder, LootTable lootTable, List list, Iterator var7, ItemStack itemStack) {
        if (!playerEntity.world.isClient) {
            if (itemStack.getItem().isIn(ItemTags.FISHES)) {
                RPGStats.addXpAndLevelUp(RPGStats.FISHING_COMPONENT, (ServerPlayerEntity)playerEntity, 3);
            } else  {
                RPGStats.addXpAndLevelUp(RPGStats.FISHING_COMPONENT, (ServerPlayerEntity)playerEntity, 1);
            }
        }
    }
    
    @Inject(method = "tickFishingLogic", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;up()Lnet/minecraft/util/math/BlockPos;"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void speedUpFishing(BlockPos pos, CallbackInfo ci, ServerWorld serverWorld, int i) {
    
    }
}

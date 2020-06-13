package mc.rpgstats.mixin;

import mc.rpgstats.main.RPGStats;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionDrinkMixin {
    @Inject(at = @At("HEAD"), method = "finishUsing")
    private void onFinishedUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity)user : null;
        if (playerEntity instanceof ServerPlayerEntity) {
            RPGStats.addXpAndLevelUpIfNeeded(RPGStats.MAGIC_COMPONENT, ComponentProvider.fromEntity(user), 10);
        }
    }

    @Inject(at = @At("HEAD"), method = "getMaxUseTime", cancellable = true)
    private void getUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Entity holder = stack.getHolder();
        System.out.println("hello world");
        System.out.println(holder);
        if (holder instanceof PlayerEntity) {
            PlayerEntity spe = (PlayerEntity)holder;
            cir.setReturnValue((int) (32 - Math.floor(RPGStats.getComponentLevel(RPGStats.MAGIC_COMPONENT, ComponentProvider.fromEntity(spe)) / 3.0f)));
            System.out.println(cir.getReturnValue());
        }
    }
}

package mc.rpgstats.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import java.util.List;
import java.util.Set;

import static net.minecraft.block.CropBlock.AGE;

@SuppressWarnings("unused")
@Mixin(CropBlock.class)
public class MaxvarReapCompatMixin extends PlantBlock implements IMixinConfigPlugin {
    
    protected MaxvarReapCompatMixin(Settings settings) {
        super(settings);
    }
    
    @Override
    public void onLoad(String mixinPackage) {}
    
    @Override
    public String getRefMapperConfig() {
        return null;
    }
    
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return FabricLoader.getInstance().isModLoaded("mcf-reap");
    }
    
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    
    @Override
    public List<String> getMixins() { return null; }
    
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
    
    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
    
    @SuppressWarnings({"SameReturnValue", "unused"})
    @Shadow
    public boolean isMature(BlockState state) {
        return true;
    }
    
    @SuppressWarnings("SameReturnValue")
    @Shadow
    public IntProperty getAgeProperty() {
        return AGE;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient() & isMature(state)) {
            
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }
}

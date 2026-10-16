package thaumcraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Modern block states cache hardness; TC4's protection setting must remain live. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class OwnedBlockHardnessMixin {
    @Inject(method="getDestroySpeed",at=@At("HEAD"),cancellable=true)
    private void thaumcraft$ownedHardness(BlockGetter level,BlockPos pos,CallbackInfoReturnable<Float> callback){if(thaumcraft.config.ThaumcraftConfig.WARDED_STONE.get()&&thaumcraft.blocks.OwnedBlockInteractions.protectedBlock(((BlockBehaviour.BlockStateBase)(Object)this).getBlock()))callback.setReturnValue(-1f);}
}

package thaumcraft.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.blocks.EldritchVoidBlock;

/** Native EntityBlock is a class-wide flag; TC4's void tile belongs only to metadata1 (exposed). */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class EldritchVoidStateMixin {
    @Inject(method="hasBlockEntity",at=@At("HEAD"),cancellable=true)
    private void thaumcraft$exposedVoidTile(CallbackInfoReturnable<Boolean> callback){
        var state=(BlockState)(Object)this;
        if(state.getBlock() instanceof EldritchVoidBlock&&!state.getValue(EldritchVoidBlock.EXPOSED))callback.setReturnValue(false);
    }
}

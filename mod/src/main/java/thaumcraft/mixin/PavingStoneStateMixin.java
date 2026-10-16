package thaumcraft.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.blocks.PavingStoneBlock;

/** The shared paving block has a tile only for the warding variant (original metadata 3). */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class PavingStoneStateMixin {
    @Inject(method="hasBlockEntity",at=@At("HEAD"),cancellable=true)
    private void thaumcraft$wardingTileOnly(CallbackInfoReturnable<Boolean> callback){
        if(((BlockState)(Object)this).getBlock() instanceof PavingStoneBlock paving&&!paving.warding)callback.setReturnValue(false);
    }
}

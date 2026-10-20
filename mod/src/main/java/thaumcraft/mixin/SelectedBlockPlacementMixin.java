package thaumcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import thaumcraft.addon.SelectedBlockPlacement;

@Mixin(BlockItem.class)
public abstract class SelectedBlockPlacementMixin {
    @WrapOperation(method="place",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/BlockItem;getPlacementState(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState thaumcraft$selectedState(BlockItem item,BlockPlaceContext context,Operation<BlockState> original){
        return SelectedBlockPlacement.selectedState(item,context,()->original.call(item,context));
    }
}

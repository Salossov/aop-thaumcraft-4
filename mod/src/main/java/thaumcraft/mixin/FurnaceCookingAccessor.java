package thaumcraft.mixin;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(AbstractFurnaceBlockEntity.class)
public interface FurnaceCookingAccessor {
 @Accessor("cookingProgress") int thaumcraft$cookingProgress();
 @Accessor("cookingProgress") void thaumcraft$cookingProgress(int value);
 @Accessor("cookingTotalTime") int thaumcraft$cookingTotalTime();
}
